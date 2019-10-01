#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <pthread.h>
#include <csignal>
#include <cstring>

#define LOG_TAG "libantidebugging"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


static int global_child_pid;

// This will cause a SIGSEGV on some QEMU or be properly respected
// 32 bit AArch
/*
void tryBKPT() {
    __asm__ __volatile__ ("bkpt 255");
}
*/

// 64 bit AArch
void tryBRK() {
    __asm__ __volatile__ ("brk 255");
}

void handler_sigtrap(int signo) {
    _exit(0);
}

void handler_sigbus(int signo) {
    _exit(0);
}

void sig_handler(int signo) {
    LOGA("Received signal: %s\n", strsignal(signo));
}

void setupSigTrap() {
    // BKPT throws SIGTRAP on nexus 5 / oneplus one (and most devices)
    signal(SIGTRAP, handler_sigtrap);
    // BKPT throws SIGBUS on nexus 4
    signal(SIGBUS, handler_sigbus);
}


void *monitor_pid(void *arg) {
    int status;
    LOGA("0 Thread process: %d", global_child_pid);

    // Child status should never change.
    // child_pid status changing means tampering with process
    waitpid(global_child_pid, &status, 0);
    LOGA("1 Thread process: %d", global_child_pid);

    _exit(0); // Commit seppuku
}

void *pthread_test_call(void *arg) {
    int *number = (int *)arg;
    LOGA("Number: %d", *number);
    pthread_exit(nullptr);
}

void pthread_test() {
    pthread_t t;
    int a = 10;
    int b = 20;

    LOGA("0 YEET");
    pthread_create(&t, nullptr, pthread_test_call, (void *)&a);
    LOGA("1 YEET");
    pthread_join(t, nullptr);
    LOGA("2 YEET");
    pthread_create(&t, nullptr, pthread_test_call, (void *)&b);
    LOGA("3 YEET");
    pthread_join(t, nullptr);
    LOGA("4 YEET");
}

bool anti_debug() {
    pid_t child_pid = fork();

    if (child_pid == 0) {
        // child process
        LOGA("0 Child process: %d", child_pid);
        int ppid = getppid();
        int status;

        // attach ptrace to parent
        LOGA("1 Child process: %d", child_pid);
        if (ptrace(PTRACE_ATTACH, ppid, NULL, NULL) == 0) {
            LOGA("2 Child process: %d", child_pid);
            waitpid(ppid, &status, 0);
            LOGA("3 Child process: %d", child_pid);
            ptrace(PTRACE_CONT, ppid, NULL, NULL);
            LOGA("4 Child process: %d", child_pid);

            // listen to the parent
            while (waitpid(ppid, &status, 0)) {
                LOGA("5 Child process: %d", child_pid);
                if (WIFSTOPPED(status)) {
                    LOGA("6 Child process: %d", child_pid);
                    LOGA("6 Child sig: %d", WSTOPSIG(status));
                    ptrace(PTRACE_CONT, ppid, NULL, NULL);
                } else if (WIFSIGNALED(status)) {
                    LOGA("7 Child process: %d", child_pid);
                    LOGA("7 Child sig: %d", WTERMSIG(status));

                    // Process has exited
                    _exit(0);
                } else if (WIFEXITED(status)) {
                    LOGA("8 Child process: %d", child_pid);
                    LOGA("8 Child status: %d", WEXITSTATUS(status));

                    // Process has exited
                    _exit(0);
                } else {
                    LOGA("9 Child process: %d", child_pid);
                    // Process has exited
                    _exit(0);
                }
            }
        }
        LOGA("Fin Child process: %d", child_pid);
    } else if (child_pid == -1) {
        LOGA("Failed to fork");

        // ded
        return false;
    } else {
        global_child_pid = child_pid;
        // parent process
        LOGA("Parent process: %d", global_child_pid);

        // Start the monitoring thread
        pthread_t t;
        pthread_create(&t, nullptr, monitor_pid, nullptr);
        LOGA("Parent process: %d", global_child_pid);
    }

    return true;
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

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AntiDebuggingJniLib_antiDebuggingPTrace(JNIEnv *env, jobject obj) {
    return static_cast<jboolean>(anti_debug());
}

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_AntiDebuggingJniLib_pthreadTest(JNIEnv *env, jobject obj) {
    pthread_test();
    return static_cast<jboolean>(true);
}

JNIEXPORT jint Java_xyz_velvetmilk_testingtool_jni_AntiDebuggingJniLib_antiDebuggingQEMU(JNIEnv *env, jobject jObject) {
    int child_status;
    int status = 0;

    pid_t child_pid = fork();

    if (child_pid == 0) {
//        struct sigaction sa = {};
//        sa.sa_handler = sig_handler;
//        sigaction(SIGTRAP, &sa, nullptr);
//        sigaction(SIGBUS, &sa, nullptr);
        setupSigTrap();
        tryBRK();
    } else if (child_pid == -1) {
        status = -1;
    } else {
        int timeout = 0;
        int i = 0;
        while (waitpid(child_pid, &child_status, WNOHANG) == 0) {
            sleep(1);
            // Time could be adjusted here, though in my experience if the child has not returned instantly
            // then something has gone wrong and it is an emulated device
            if (i++ == 1) {
                timeout = 1;
                break;
            }
        }

        if (timeout == 1) {
            // Process timed out - likely an emulated device and child is frozen
            status = 1;
        } else if (WIFEXITED(child_status)) {
            // Likely a real device
            status = 0;
        } else {
            // Didn't exit properly - very likely an emulator
            status = 2;
        }

        // Ensure child is dead
        kill(child_pid, SIGKILL);
    }

    return status;
}

__attribute__ ((visibility ("default"))) void dynamic_test() {
    LOGA("DYNAMICALLY CALLED\n");
}
}
