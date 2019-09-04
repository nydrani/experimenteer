#include <jni.h>
#include <aaudio/AAudio.h>
#include <android/log.h>
#include <string>
#include <android/api-level.h>

#define LOG_TAG "libaaudiotest"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)


// global items
static AAudioStream *stream = nullptr;

void customErrorCallback(AAudioStream *stream, void *userData, aaudio_result_t error) {
    LOGA("AAudioErrorCallback %s", AAudio_convertResultToText(error));
}

aaudio_data_callback_result_t customDataCallback(AAudioStream *stream,
                                                 void *userData,
                                                 void *audioData,
                                                 int32_t numFrames) {
    LOGA("AAudioDataCallback | numFrames: %d", numFrames);
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
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
JNIEXPORT void JNICALL Java_xyz_velvetmilk_aaudiotester_jni_AAudioJniLib_createEngine(JNIEnv *env,  jobject obj)
{
    // stream is already opem
    if (stream != nullptr) {
        return;
    }

    AAudioStreamBuilder *builder;

    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    LOGA("AAudio_createStreamBuilder %s", AAudio_convertResultToText(result));

    AAudioStreamBuilder_setBufferCapacityInFrames(builder, AAUDIO_UNSPECIFIED);
    AAudioStreamBuilder_setDataCallback(builder, &customDataCallback, nullptr);
    AAudioStreamBuilder_setDeviceId(builder, AAUDIO_UNSPECIFIED);
    AAudioStreamBuilder_setDirection(builder, AAUDIO_DIRECTION_OUTPUT);
    AAudioStreamBuilder_setErrorCallback(builder, &customErrorCallback, nullptr);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_UNSPECIFIED);
//    unnecessary call
//    AAudioStreamBuilder_setFramesPerDataCallback(builder, AAUDIO_UNSPECIFIED);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setSampleRate(builder, AAUDIO_UNSPECIFIED);
//    same as setChannelCount
//    AAudioStreamBuilder_setSamplesPerFrame(builder, sampleRate);
    AAudioStreamBuilder_setSharingMode(builder, AAUDIO_SHARING_MODE_EXCLUSIVE);

    // API 28
#if __ANDROID_API__ >= __ANDROID_API_P__
    AAudioStreamBuilder_setContentType(builder, AAUDIO_CONTENT_TYPE_MUSIC);
    AAudioStreamBuilder_setChannelCount(builder, AAUDIO_UNSPECIFIED);
    AAudioStreamBuilder_setInputPreset(builder, AAUDIO_INPUT_PRESET_VOICE_PERFORMANCE);
    AAudioStreamBuilder_setSessionId(builder, AAUDIO_SESSION_ID_ALLOCATE);
    AAudioStreamBuilder_setUsage(builder, AAUDIO_USAGE_MEDIA);
#endif

    // API 29
#if __ANDROID_API__ >= __ANDROID_API_Q__
    AAudioStreamBuilder_setAllowedCapturePolicy(builder, AAUDIO_ALLOW_CAPTURE_BY_ALL);
#endif

    result = AAudioStreamBuilder_openStream(builder, &stream);
    LOGA("AAudioStreamBuilder_openStream %s", AAudio_convertResultToText(result));

    AAudioStreamBuilder_delete(builder);
}

JNIEXPORT void JNICALL Java_xyz_velvetmilk_aaudiotester_jni_AAudioJniLib_shutdown(JNIEnv *env, jobject obj)
{
    aaudio_result_t result = AAudioStream_close(stream);
    LOGA("AAudioStreamBuilder_close %s", AAudio_convertResultToText(result));
}

JNIEXPORT void JNICALL Java_xyz_velvetmilk_aaudiotester_jni_AAudioJniLib_getState(JNIEnv *env, jobject obj)
{
    aaudio_stream_state_t state = AAudioStream_getState(stream);
    LOGA("AAudioStream_getState %s", AAudio_convertStreamStateToText(state));
}

JNIEXPORT void JNICALL Java_xyz_velvetmilk_aaudiotester_jni_AAudioJniLib_play(JNIEnv *env, jobject obj)
{
    aaudio_stream_state_t state = AAudioStream_requestStart(stream);
    // play start playing
}

JNIEXPORT void JNICALL Java_xyz_velvetmilk_aaudiotester_jni_AAudioJniLib_pause(JNIEnv *env, jobject obj)
{
    aaudio_stream_state_t state = AAudioStream_requestPause(stream);
}

JNIEXPORT void JNICALL Java_xyz_velvetmilk_aaudiotester_jni_AAudioJniLib_stop(JNIEnv *env, jobject obj)
{
    aaudio_stream_state_t state = AAudioStream_requestStop(stream);
}
}
