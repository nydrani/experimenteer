#include <jni.h>
#include <android/log.h>
#include <string>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>


#define LOG_TAG "libeaves"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)


// engine interfaces
static SLObjectItf engineObject = nullptr;
static SLEngineItf engineEngine = nullptr;

static SLObjectItf outputMixObject = nullptr;

static SLObjectItf playerObject = nullptr;
static SLPlayItf playerPlay = nullptr;
static SLAndroidSimpleBufferQueueItf playerBufferQueue = nullptr;

static SLObjectItf recorderObject = nullptr;
static SLRecordItf recorderRecord = nullptr;
static SLAndroidSimpleBufferQueueItf recorderBufferQueue = nullptr;

// global ints for recorder
// 10 seconds of recorded audio at 44.1 kHz stereo, 16-bit signed little endian (2channel)
#define RECORDED_SECONDS 10
#define BITS_PER_DECISECOND (4410 * 2)
#define BITS_PER_SECOND (BITS_PER_DECISECOND * 10)
#define RECORDER_FRAMES (BITS_PER_SECOND * RECORDED_SECONDS)
//constexpr uint8_t recordedSeconds = 10;
//constexpr uint16_t bitsPerDecisecond = 4410 * 2;
//constexpr uint32_t bitsPerSecond = bitsPerDecisecond * 10;
//constexpr uint32_t recorderFrames = bitsPerSecond * recordedSeconds;

static int16_t recorderBuffer[RECORDER_FRAMES];
static int32_t recorderSize = 0;


// this callback handler is called every time a buffer finishes recording
void playerCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    assert(bq == playerBufferQueue);
    assert(context == nullptr);

    LOGA("Player callback fired");
    SLresult result;
    result = (*playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_STOPPED);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;
}

// this callback handler is called every time a buffer finishes recording
void recorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    assert(bq == recorderBufferQueue);
    assert(context == nullptr);

    LOGA("Recorder callback fired");
    // for streaming recording, here we would call Enqueue to give recorder the next buffer to fill
    // but instead, this is a one-time buffer so we stop recording
    SLresult result;
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    if (result == SL_RESULT_SUCCESS) {
        recorderSize = RECORDER_FRAMES * sizeof(int16_t);
    }

    // query the record position
    SLmillisecond recordPosition = 0;

    result = (*recorderRecord)->GetPosition(recorderRecord, &recordPosition);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;
    LOGA("GetPosition: %u", recordPosition);
}

extern "C" {
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {

    // do nothing lol
}

// create the engine and output mix objects
JNIEXPORT void JNICALL Java_xyz_velvetmilk_testingtool_jni_EavesJNILib_createEngine(JNIEnv* env, jclass clazz)
{
    SLresult result;

    // create engine
    SLEngineOption engineOption[] = {{SL_ENGINEOPTION_THREADSAFE, SL_BOOLEAN_TRUE}};
    result = slCreateEngine(&engineObject, 1, engineOption, 0, nullptr, nullptr);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // create output mix, with environmental reverb specified as a non-required interface
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, nullptr, nullptr);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;
}

// create audio recorder: recorder is not in fast path
// like to avoid excessive re-sampling while playing back from Hello & Android clip
JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_EavesJNILib_createAudioRecorder(JNIEnv* env, jclass clazz)
{
    SLresult result;

    // configure audio source
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE,
                                      SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT,
                                      nullptr};
    SLDataSource audioSrc = {&loc_dev, nullptr};

    // configure audio sink
    SLDataLocator_AndroidSimpleBufferQueue bufferQueue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM,
                            2,
                            SL_SAMPLINGRATE_44_1,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            16,
                            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                            SL_BYTEORDER_LITTLEENDIAN};
    SLDataSink audioSnk = {&bufferQueue, &pcm};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    SLboolean req[1] = {SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioRecorder(engineEngine, &recorderObject, &audioSrc, &audioSnk, 1, id, req);
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    // realize the audio recorder
    result = (*recorderObject)->Realize(recorderObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    // get the record interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_RECORD, &recorderRecord);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // get the buffer queue interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &recorderBufferQueue);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // register callback on the buffer queue
    result = (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, recorderCallback, nullptr);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    return JNI_TRUE;
}

// create audio recorder: recorder is not in fast path
// like to avoid excessive re-sampling while playing back from Hello & Android clip
JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_EavesJNILib_createAudioPlayer(JNIEnv* env, jclass clazz)
{
    SLresult result;

    // configure audio source
    SLDataLocator_AndroidSimpleBufferQueue bufferQueue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM,
                            2,
                            SL_SAMPLINGRATE_44_1,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            16,
                            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                            SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&bufferQueue, &pcm};

    SLDataLocator_OutputMix outMixLocator = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};

    // configure audio sink
    SLDataSink audioSnk = {&outMixLocator, nullptr};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    SLboolean req[1] = {SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &playerObject, &audioSrc, &audioSnk, 1, id, req);
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    // realize the audio recorder
    result = (*playerObject)->Realize(playerObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    // get the record interface
    result = (*playerObject)->GetInterface(playerObject, SL_IID_PLAY, &playerPlay);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // get the buffer queue interface
    result = (*playerObject)->GetInterface(playerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &playerBufferQueue);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // register callback on the buffer queue
    result = (*playerBufferQueue)->RegisterCallback(playerBufferQueue, playerCallback, nullptr);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    return JNI_TRUE;
}

// set the recording state for the audio recorder
JNIEXPORT void JNICALL Java_xyz_velvetmilk_testingtool_jni_EavesJNILib_startPlaying(JNIEnv* env, jclass clazz)
{
    SLresult result;

    // in case already recording, stop recording and clear buffer queue
    result = (*playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_STOPPED);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // enqueue an empty buffer to be played by the player
    result = (*playerBufferQueue)->Enqueue(playerBufferQueue, recorderBuffer,
                                             RECORDER_FRAMES * sizeof(short));
    // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
    // which for this code example would indicate a programming error
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // start playing
    result = (*playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_PLAYING);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;
}

// set the recording state for the audio recorder
JNIEXPORT void JNICALL Java_xyz_velvetmilk_testingtool_jni_EavesJNILib_startRecording(JNIEnv* env, jclass clazz)
{
    SLresult result;

//    if (pthread_mutex_trylock(&audioEngineLock)) {
//        return;
//    }

    // in case already recording, stop recording and clear buffer queue
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    result = (*recorderBufferQueue)->Clear(recorderBufferQueue);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // the buffer is not valid for playback yet
    recorderSize = 0;

    // enqueue an empty buffer to be filled by the recorder
    // (for streaming recording, we would enqueue at least 2 empty buffers to start things off)
    result = (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recorderBuffer,
                                             RECORDER_FRAMES * sizeof(short));
    // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
    // which for this code example would indicate a programming error
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // start recording
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_RECORDING);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;
}

// set the recording state for the audio recorder
JNIEXPORT void JNICALL Java_xyz_velvetmilk_testingtool_jni_EavesJNILib_stopRecording(JNIEnv* env, jclass clazz)
{
    SLresult result;

//    if (pthread_mutex_trylock(&audioEngineLock)) {
//        return;
//    }

    // in case already recording, stop recording and clear buffer queue
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    // query the record position
    SLmillisecond recordPosition = 0;

    result = (*recorderRecord)->GetPosition(recorderRecord, &recordPosition);
    assert(result == SL_RESULT_SUCCESS);
    (void)result;
    LOGA("GetPosition: %u", recordPosition);

    // NOTE: We cut round down to the closest centisecond due to OpenSLES GetPosition() being inaccurate
    recorderSize = recordPosition / 100 * BITS_PER_DECISECOND * sizeof(int16_t);
}

// set the recording state for the audio recorder
JNIEXPORT jbyteArray JNICALL Java_xyz_velvetmilk_testingtool_jni_EavesJNILib_getRecordingData(JNIEnv* env, jclass clazz)
{
    jbyteArray array = env->NewByteArray(recorderSize);
    env->SetByteArrayRegion(array, 0, recorderSize, reinterpret_cast<const jbyte*>(recorderBuffer));

    return array;
}

// shut down the native audio system
JNIEXPORT void JNICALL Java_xyz_velvetmilk_testingtool_jni_EavesJNILib_shutdown(JNIEnv* env, jclass clazz)
{
    // destroy audio recorder object, and invalidate all associated interfaces
    if (recorderObject != nullptr) {
        (*recorderObject)->Destroy(recorderObject);
        recorderObject = nullptr;
        recorderRecord = nullptr;
        recorderBufferQueue = nullptr;
    }

    if (playerObject != nullptr) {
        (*playerObject)->Destroy(playerObject);
        playerObject = nullptr;
        playerPlay = nullptr;
        playerBufferQueue = nullptr;
    }

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != nullptr) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = nullptr;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != nullptr) {
        (*engineObject)->Destroy(engineObject);
        engineObject = nullptr;
        engineEngine = nullptr;
    }
}
}
