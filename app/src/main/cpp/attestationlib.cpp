#include <jni.h>
#include <android/log.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <cerrno>
#include <cstdio>
#include <unistd.h>
#include <cstring>
#include <dirent.h>
#include <sys/system_properties.h>
#include <sys/wait.h>
#include <cstdlib>

#define LOG_TAG "libattestation"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// build.props
const char *const ANDROID_OS_BUILD_VERSION_RELEASE = "ro.build.version.release";
const char *const ANDROID_OS_BUILD_VERSION_INCREMENTAL = "ro.build.version.incremental";
const char *const ANDROID_OS_BUILD_VERSION_CODENAME = "ro.build.version.codename";
const char *const ANDROID_OS_BUILD_VERSION_SDK = "ro.build.version.sdk";

const char *const ANDROID_OS_BUILD_MODEL = "ro.product.model";
const char *const ANDROID_OS_BUILD_MANUFACTURER = "ro.product.manufacturer";
const char *const ANDROID_OS_BUILD_BOARD = "ro.product.board";
const char *const ANDROID_OS_BUILD_BRAND = "ro.product.brand";
const char *const ANDROID_OS_BUILD_DEVICE = "ro.product.device";
const char *const ANDROID_OS_BUILD_PRODUCT = "ro.product.name";

const char *const ANDROID_OS_BUILD_CPU_ABI = "ro.product.cpu.abi";
const char *const ANDROID_OS_BUILD_CPU_ABI2 = "ro.product.cpu.abi2";

const char *const ANDROID_OS_BUILD_HARDWARE = "ro.hardware";

const char *const ANDROID_OS_BUILD_ID = "ro.build.id";
const char *const ANDROID_OS_BUILD_DISPLAY = "ro.build.display.id";
const char *const ANDROID_OS_BUILD_HOST = "ro.build.host";
const char *const ANDROID_OS_BUILD_USER = "ro.build.user";
const char *const ANDROID_OS_BUILD_TYPE = "ro.build.type";
const char *const ANDROID_OS_BUILD_TAGS = "ro.build.tags";
const char *const ANDROID_OS_BUILD_FINGERPRINT = "ro.build.fingerprint";

const char *const ANDROID_OS_SECURE = "ro.secure";
const char *const ANDROID_OS_DEBUGGABLE = "ro.debuggable";
const char *const ANDROID_OS_BUILD_SELINUX = "ro.build.selinux";

const char *const ANDROID_OS_SYS_INITD = "sys.initd";
const char *const SERVICE_ADB_ROOT = "service.adb.root";


const char* const MG_SU_PATH[] = {
        "/data/local/",
        "/data/local/bin/",
        "/data/local/xbin/",
        "/sbin/",
        "/system/bin/",
        "/system/bin/.ext/",
        "/system/bin/failsafe/",
        "/system/sd/xbin/",
        "/su/xbin/",
        "/su/bin/",
        "/magisk/.core/bin/",
        "/system/usr/we-need-root/",
        "/system/xbin/"
};

const char* const MG_EXPOSED_FILES[] = {
        "/system/lib/libxposed_art.so",
        "/system/lib64/libxposed_art.so",
        "/system/xposed.prop",
        "/cache/recovery/xposed.zip",
        "/system/framework/XposedBridge.jar",
        "/system/bin/app_process64_xposed",
        "/system/bin/app_process32_xposed",
        "/magisk/xposed/system/lib/libsigchain.so",
        "/magisk/xposed/system/lib/libart.so",
        "/magisk/xposed/system/lib/libart-disassembler.so",
        "/magisk/xposed/system/lib/libart-compiler.so",
        "/system/bin/app_process32_orig",
        "/system/bin/app_process64_orig"
};

const char* const MG_READ_ONLY_PATH[] = {
        "/system",
        "/system/bin",
        "/system/sbin",
        "/system/xbin",
        "/vendor/bin",
        "/sbin",
        "/etc"
};


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

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_nativeFileStat(JNIEnv* env, jobject obj) {
    struct stat file_info = {};

    const char *fname_list[2];
    fname_list[0] = "/sbin/magisk";
    fname_list[1] = "/sbin/magisk.db";

    for (auto fname : fname_list) {
        if (stat(fname, &file_info) == 0) {
            LOGA("%d", file_info.st_mode);
            return static_cast<jboolean>(true);
        } else {
            LOGE("Error: %s", strerror(errno));
        }
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_makeDirectory(JNIEnv* env, jobject obj) {
    const char *fname_list[2];
    fname_list[0] = "/sbin/magisk";
    fname_list[1] = "/sbin/su";

    for (auto fname : fname_list) {
        if (mkdir(fname, S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IROTH) == 0) {
            return static_cast<jboolean>(true);
        } else {
            LOGE("Error: %s", strerror(errno));
        }
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_changeDirectory(JNIEnv* env, jobject obj) {
    const char *fname = "/sbin/magisk";

    if (chdir(fname) == 0) {
        return static_cast<jboolean>(true);
    } else {
        LOGE("Error: %s", strerror(errno));
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_openDirectory(JNIEnv* env, jobject obj) {
    const char *fname = "/sbin/magisk";

    DIR *opened = opendir(fname);
    if (opened != nullptr) {
        closedir(opened);
        return static_cast<jboolean>(true);
    } else {
        LOGE("Error: %s", strerror(errno));
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_accessDirectory(JNIEnv* env, jobject obj) {
    const char *fname_list[2];
    fname_list[0] = "/sbin/magisk";
    fname_list[1] = "/sbin/su";


    for (auto fname : fname_list) {
        if (access(fname, F_OK) == 0) {
            LOGA("Access allowed: %s", fname);
            return static_cast<jboolean>(true);
        } else {
            LOGE("Error: %s", strerror(errno));
        }
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_lstatDirectory(JNIEnv* env, jobject obj) {
    const char *fname_list[2];
    fname_list[0] = "/sbin/magisk";
    fname_list[1] = "/sbin/su";

    struct stat sb = {};

    for (auto fname : fname_list) {
        if (lstat(fname, &sb) == 0) {
            LOGA("Lstat allowed: %s", fname);
            return static_cast<jboolean>(true);
        } else {
            LOGE("Error: %s", strerror(errno));
        }
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_getEnvironVariables(JNIEnv* env, jobject obj) {
    for (char **env_item = environ; *env_item != nullptr; ++env_item) {
        LOGA("%s\n", *env_item);
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_checkMemoryMap(JNIEnv* env, jobject obj) {
    char buf[BUFSIZ];

    FILE* pipe = fopen("/proc/self/maps", "re");

    if (pipe == nullptr) {
        LOGE("cant open /proc/self/maps\n");
        return static_cast<jboolean>(false);
    }

    while (fgets(buf, BUFSIZ, pipe) != nullptr) {
        if (strstr(buf, "frida") || strstr(buf, "xposed") || strstr(buf, "Xposed") || strstr(buf, "substrate")) {
            fclose(pipe);
            return static_cast<jboolean>(true);
        }
    }

    fclose(pipe);

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_callPopen(JNIEnv* env, jobject obj) {
    char buf[BUFSIZ];
    size_t size = 0;

    FILE *pipe = popen("which su", "r");
    if (pipe == nullptr) {
        LOGE("which su failed\n");
        return static_cast<jboolean>(false);
    }

    while (fgets(buf, BUFSIZ, pipe) != nullptr) {
        LOGA("Buffer: %s\n", buf);
        size += strlen(buf);
    }

    pclose(pipe);
    LOGA("Size of the result from pipe is [%zu]\n", size);

    return static_cast<jboolean>(size != 0);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_callProcessList(JNIEnv* env, jobject obj) {
    char buf[BUFSIZ];
    size_t size = 0;

    FILE *pipe = popen("ps -AZ", "r");
    if (pipe == nullptr) {
        LOGE("ps -A failed\n");
        return static_cast<jboolean>(false);
    }

    while (fgets(buf, BUFSIZ, pipe) != nullptr) {
        LOGA("Buffer: %s\n", buf);
        size += strlen(buf);
    }

    pclose(pipe);
    LOGA("Size of the result from pipe is [%zu]\n", size);

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_callDmesg(JNIEnv* env, jobject obj) {
    char buf[BUFSIZ];
    size_t size = 0;

    FILE *pipe = popen("dmesg", "r");
    if (pipe == nullptr) {
        LOGE("dmesg failed\n");
        return static_cast<jboolean>(false);
    }

    while (fgets(buf, BUFSIZ, pipe) != nullptr) {
        if (strstr(buf, "magisk") != nullptr) {
            pclose(pipe);
            return static_cast<jboolean>(true);
        }
        LOGA("Buffer: %s\n", buf);
        size += strlen(buf);
    }

    pclose(pipe);
    LOGA("Size of the result from pipe is [%zu]\n", size);

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_callSystemSh(JNIEnv* env, jobject obj) {
    char buf[BUFSIZ];
    size_t size = 0;

    FILE *pipe = popen("whoami", "r");
    if (pipe == nullptr) {
        LOGE("whoami failed\n");
        return static_cast<jboolean>(false);
    }

    while (fgets(buf, BUFSIZ, pipe) != nullptr) {
        LOGA("Buffer: %s\n", buf);
        size += strlen(buf);
    }

    pclose(pipe);
    LOGA("Size of the result from pipe is [%zu]\n", size);

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_callFork(JNIEnv* env, jobject obj) {
    char buf[BUFSIZ];
    size_t size = 0;
    int status;

    int pipefd[2];
    pipe(pipefd);

    pid_t pid = fork();
    if (pid == 0) {
        // child
        LOGA("CHILD %d, %d", getpid(), getppid());

        close(pipefd[0]);    // close reading end in the child

        dup2(pipefd[1], 1);  // send stdout to the pipe
        dup2(pipefd[1], 2);

        close(pipefd[1]);    // this descriptor is no longer needed

        char* const argv[] = { "/system/bin/sh", "-c", "/system/bin/cat /system/build.prop", 0 };
        char* const envp[] = { "PATH=/sbin", 0 };
        LOGA("CHILD yeet");
        LOGA("CHILD: %d", execve(argv[0], &argv[0], envp));
    } else {
        // parent
        LOGA("PARENT %d, %d", getpid(), getppid());

        char buffer[BUFSIZ] = {0};

        close(pipefd[1]);  // close the write end of the pipe in the parent

        while (read(pipefd[0], buffer, sizeof(buffer) - 1) != 0) {
            LOGA("CHILD WHATTHEHHHHHH: %s", buffer);
        }
        waitpid(pid, &status, 0);

        close(pipefd[0]);  // close the write end of the pipe in the parent
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_openProcDirectory(JNIEnv* env, jobject obj) {
    const char *fname = "/proc/self";

    DIR* opened = opendir(fname);
    if (opened == nullptr) {
        LOGE("Error: %s", strerror(errno));
        return static_cast<jboolean>(false);
    }

    struct dirent *dirent;
    while ((dirent = readdir(opened)) != nullptr) {
        LOGA("[%s]\n", dirent->d_name);
    }

    closedir(opened);
    return static_cast<jboolean>(false);
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_checkSystemProperties(JNIEnv* env, jobject obj) {
    const char* const ANDROID_OS_BUILD_TAGS = "ro.build.tags";
    const char* const ANDROID_OS_BUILD_FINGERPRINT = "ro.build.fingerprint";
    const char* const ANDROID_OS_BUILD_SELINUX = "ro.build.selinux";
    const char* const ANDROID_OS_BUILD_TYPE = "ro.build.type";
    const char* const ANDROID_OS_DEBUGGABLE = "ro.debuggable";
    const char* const ANDROID_OS_SECURE = "ro.secure";
    const char* const ANDROID_OS_SYS_INITD = "sys.initd";
    const char* const SERVICE_ADB_ROOT = "service.adb.root";

    const char* prop_list[] = { ANDROID_OS_BUILD_FINGERPRINT,
                                ANDROID_OS_BUILD_SELINUX,
                                ANDROID_OS_BUILD_TYPE,
                                ANDROID_OS_DEBUGGABLE,
                                ANDROID_OS_SECURE,
                                ANDROID_OS_SYS_INITD,
                                ANDROID_OS_BUILD_TAGS,
                                SERVICE_ADB_ROOT };

    char value[PROP_VALUE_MAX + 1];

    for (auto& prop : prop_list) {
        memset(value, 0, PROP_NAME_MAX + 1);
        int length = __system_property_get(prop, value);
        if (length == 0) {
            LOGA("%s not found\n", prop);
        } else {
            LOGA("%s: %s\n", prop, value);
        }
    }

    return static_cast<jboolean>(false);
}

JNIEXPORT jobject JNICALL Java_xyz_velvetmilk_testingtool_jni_AttestationJniLib_collectSystemProperties(JNIEnv* env, jobject obj) {
    jclass mapClass = env->FindClass("java/util/HashMap");
    jmethodID mapInit = env->GetMethodID(mapClass, "<init>", "()V");
    jmethodID putMethod = env->GetMethodID(mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    jobject hashMap = env->NewObject(mapClass, mapInit);

    const char* prop_list[] = { ANDROID_OS_BUILD_FINGERPRINT,
                                ANDROID_OS_BUILD_VERSION_RELEASE,
                                ANDROID_OS_BUILD_VERSION_INCREMENTAL,
                                ANDROID_OS_BUILD_VERSION_CODENAME,
                                ANDROID_OS_BUILD_VERSION_SDK,

                                ANDROID_OS_BUILD_MODEL,
                                ANDROID_OS_BUILD_MANUFACTURER,
                                ANDROID_OS_BUILD_BOARD,
                                ANDROID_OS_BUILD_BRAND,
                                ANDROID_OS_BUILD_DEVICE,
                                ANDROID_OS_BUILD_PRODUCT,

                                ANDROID_OS_BUILD_CPU_ABI,
                                ANDROID_OS_BUILD_CPU_ABI2,

                                ANDROID_OS_BUILD_HARDWARE,

                                ANDROID_OS_BUILD_ID,
                                ANDROID_OS_BUILD_DISPLAY,
                                ANDROID_OS_BUILD_HOST,
                                ANDROID_OS_BUILD_USER,
                                ANDROID_OS_BUILD_TYPE,
                                ANDROID_OS_BUILD_TAGS,
                                ANDROID_OS_BUILD_FINGERPRINT,

                                ANDROID_OS_SECURE,
                                ANDROID_OS_DEBUGGABLE,
                                ANDROID_OS_BUILD_SELINUX,

                                ANDROID_OS_SYS_INITD,
                                SERVICE_ADB_ROOT };

    char value[PROP_VALUE_MAX + 1];

    for (auto& prop : prop_list) {
        memset(value, 0, PROP_NAME_MAX + 1);
        __system_property_get(prop, value);
        env->CallObjectMethod(hashMap, putMethod, env->NewStringUTF(prop), env->NewStringUTF(value));
    }

    return hashMap;
}
}
