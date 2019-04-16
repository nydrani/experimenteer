#include <jni.h>
#include <android/log.h>
#include <string>


#define LOG_TAG "libtest"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)


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

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_TestingJNILib_nativeString(JNIEnv* env, jobject obj) {
    std::string hello = "(string) Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_TestingJNILib_nativeByteArray(JNIEnv* env, jobject obj, jbyteArray byteArray) {
    auto arrayLen = static_cast<u_int32_t>(env->GetArrayLength(byteArray));

    jbyte buffer[arrayLen];
    env->GetByteArrayRegion(byteArray, 0, arrayLen, buffer);

    const char* cstring = reinterpret_cast<const char*>(buffer);
    std::string byteString(cstring, arrayLen);

    jstring string = env->NewStringUTF(byteString.c_str());

    return string;
}

JNIEXPORT jbyteArray JNICALL Java_xyz_velvetmilk_testingtool_TestingJNILib_nativeToByteArray(JNIEnv* env, jobject obj, jstring string) {
    auto strLength = static_cast<u_int32_t>(env->GetStringUTFLength(string));

    char buffer[strLength];
    env->GetStringUTFRegion(string, 0, strLength, buffer);

    jbyteArray array = env->NewByteArray(strLength);
    env->SetByteArrayRegion(array, 0, strLength, reinterpret_cast<const jbyte*>(buffer));

    return array;
}
}
