#include <jni.h>
#include <android/log.h>
#include <string>
#include <dlfcn.h>

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

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeString(JNIEnv* env, jobject obj) {
    std::string hello = "(string) Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeByteArray(JNIEnv* env, jobject obj, jbyteArray byteArray) {
    auto arrayLen = static_cast<u_int32_t>(env->GetArrayLength(byteArray));

    jbyte buffer[arrayLen];
    env->GetByteArrayRegion(byteArray, 0, arrayLen, buffer);

    const char* cstring = reinterpret_cast<const char*>(buffer);
    std::string byteString(cstring, arrayLen);

    jstring string = env->NewStringUTF(byteString.c_str());

    return string;
}

JNIEXPORT jbyteArray JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeToByteArray(JNIEnv* env, jobject obj, jstring string) {
    auto strLength = static_cast<u_int32_t>(env->GetStringUTFLength(string));

    char buffer[strLength];
    env->GetStringUTFRegion(string, 0, strLength, buffer);

    jbyteArray array = env->NewByteArray(strLength);
    env->SetByteArrayRegion(array, 0, strLength, reinterpret_cast<const jbyte*>(buffer));

    return array;
}

JNIEXPORT jstring JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeGrabSha256(JNIEnv* env, jobject obj, jstring string) {
    auto strLength = static_cast<u_int32_t>(env->GetStringUTFLength(string));

    char buffer[strLength];
    env->GetStringUTFRegion(string, 0, strLength, buffer);

    char command[] = "sha256sum ";
    char command_buffer[strLength+sizeof(command)/sizeof(command[0])];

    strcat(command_buffer, command);
    strcat(command_buffer, buffer);

    int result = system(command_buffer);
    LOGA("%d\n", result);

    return string;
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_TestingJniLib_nativeTestDlSym(JNIEnv* env, jobject obj, jstring string) {
    void *handle;
    void (*func_dynamic_call)();

    handle = dlopen("libantidebugging.so", RTLD_LAZY);
    if (handle == nullptr) {
        /* fail to load the library */
        LOGA("Error: %s\n", dlerror());
        return static_cast<jboolean>(false);
    }

    *(void**)(&func_dynamic_call) = dlsym(handle, "dynamic_test");
    if (func_dynamic_call == nullptr) {
        /* no such symbol */
        LOGA("Error: %s\n", dlerror());
        dlclose(handle);
        return static_cast<jboolean>(false);
    }

    func_dynamic_call();
    dlclose(handle);

    return static_cast<jboolean>(true);
}
}
