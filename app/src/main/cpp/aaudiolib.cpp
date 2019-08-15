#include <jni.h>
#include <aaudio/AAudio.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "libeaves"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)


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
JNIEXPORT void JNICALL Java_xyz_velvetmilk_testingtool_jni_AAudioJniLib_createEngine(JNIEnv *env,  jobject obj)
{
    AAudioStreamBuilder *builder;

    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    LOGA("AAudio_createStreamBuilder %s", AAudio_convertResultToText(result));

    AAudioStreamBuilder_setBufferCapacityInFrames(builder, AAUDIO_UNSPECIFIED);
    AAudioStreamBuilder_setContentType(builder, AAUDIO_CONTENT_TYPE_MUSIC);
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
    AAudioStreamBuilder_setChannelCount(builder, AAUDIO_UNSPECIFIED);
    AAudioStreamBuilder_setInputPreset(builder, AAUDIO_INPUT_PRESET_VOICE_PERFORMANCE);
    AAudioStreamBuilder_setSessionId(builder, AAUDIO_SESSION_ID_ALLOCATE);
    AAudioStreamBuilder_setUsage(builder, AAUDIO_USAGE_MEDIA);

    // API 29
    AAudioStreamBuilder_setAllowedCapturePolicy(builder, AAUDIO_ALLOW_CAPTURE_BY_ALL);

    AAudioStreamBuilder_delete(builder);
}

JNIEXPORT void JNICALL Java_xyz_velvetmilk_testingtool_jni_AAudioJniLib_shutdown(JNIEnv *env, jobject obj)
{
}
}
