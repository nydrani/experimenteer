#include <jni.h>
#include <android/log.h>
#include <string>
#include <vulkan/vulkan.h>


#define LOG_TAG "libvulkantest"
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

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_jni_VulkanJniLib_nativeString(JNIEnv* env, jobject obj) {
    std::string hello = "(string) Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
}
