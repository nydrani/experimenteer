#include <jni.h>
#include <android/log.h>
#include <sys/stat.h>
#include <cerrno>
#include <cstdio>


#define LOG_TAG "libattestation"
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

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_AttestationJNILib_nativeFileStat(JNIEnv* env, jobject obj) {
    struct stat file_info = { 0 };

    const char *fname_list[2];
    fname_list[0] = "/sbin/magisk";
    fname_list[1] = "/sbin/magisk.db";

    for (auto fname : fname_list) {
        if (stat(fname, &file_info) == 0) {
            LOGA("%d", file_info.st_mode);
            return static_cast<jboolean>(true);
        } else {
            perror("Error: ");
        }
    }

    return static_cast<jboolean>(false);
}
}
