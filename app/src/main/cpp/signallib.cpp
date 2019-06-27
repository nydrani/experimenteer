#include <jni.h>
#include <android/log.h>
#include <csignal>
#include <cstring>

#define LOG_TAG "libsignal"
#define LOGA(...)  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


void print_sigset(const sigset_t *sigset) {
    int sig;
    int cnt = 0;

    for (sig = 1; sig < NSIG; sig++) {
        if (sigismember(sigset, sig)) {
            cnt++;
            LOGA("%d (%s)\n", sig, strsignal(sig));
        }
    }

    if (cnt == 0) {
        LOGA("Empty signal set\n");
    }
}


void sig_handler(int signo) {
    LOGA("Received signal: %s\n", strsignal(signo));
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

JNIEXPORT jboolean JNICALL Java_xyz_velvetmilk_testingtool_jni_SignalJNILib_setupSignalHandler(JNIEnv* env, jobject obj) {
    struct sigaction sa = {};

    sigset_t all_signals;
    sigset_t old_signals;
    sigfillset(&all_signals);

    sigprocmask(SIG_BLOCK, &all_signals, &old_signals);
    LOGA("Old signals");
    print_sigset(&old_signals);

    sigprocmask(SIG_SETMASK, &old_signals, &old_signals);
    LOGA("New signals");
    print_sigset(&old_signals);

    sa.sa_handler = sig_handler;
    //sigemptyset(&sa.sa_mask);
    //sa.sa_flags = SA_RESTART; /* Restart functions if interrupted by handler */

    // NOTE: SIGSTOP and SIGKILL cannot be overriden
    for (int i = 1; i < NSIG; ++i) {
        // Dont set signals for weird unknown signals
        if (sigismember(&all_signals, i)) {
            int res = sigaction(i, &sa, nullptr);
            LOGA("sigaction for: %s = %d", strsignal(i), res);
        }
    }

//    sigaction(SIGHUP, &sa, nullptr);
//    sigaction(SIGINT, &sa, nullptr);
//    sigaction(SIGQUIT, &sa, nullptr);
//    sigaction(SIGILL, &sa, nullptr);
//    sigaction(SIGTRAP, &sa, nullptr);
//    sigaction(SIGABRT, &sa, nullptr);
//    sigaction(SIGIOT, &sa, nullptr);
//    sigaction(SIGBUS, &sa, nullptr);
//    sigaction(SIGFPE, &sa, nullptr);
//    sigaction(SIGKILL, &sa, nullptr);
//    sigaction(SIGUSR1 , &sa, nullptr);
//    sigaction(SIGSEGV , &sa, nullptr);
//    sigaction(SIGUSR2 , &sa, nullptr);
//    sigaction(SIGPIPE , &sa, nullptr);
//    sigaction(SIGALRM , &sa, nullptr);
//    sigaction(SIGTERM , &sa, nullptr);
//    sigaction(SIGSTKFLT , &sa, nullptr);
//    sigaction(SIGCHLD , &sa, nullptr);
//    sigaction(SIGCONT , &sa, nullptr);
//    sigaction(SIGSTOP , &sa, nullptr);
//    sigaction(SIGTSTP , &sa, nullptr);
//    sigaction(SIGTTIN , &sa, nullptr);
//    sigaction(SIGTTOU , &sa, nullptr);
//    sigaction(SIGURG , &sa, nullptr);
//    sigaction(SIGXCPU , &sa, nullptr);
//    sigaction(SIGXFSZ , &sa, nullptr);
//    sigaction(SIGVTALRM , &sa, nullptr);
//    sigaction(SIGPROF , &sa, nullptr);
//    sigaction(SIGWINCH , &sa, nullptr);
//    sigaction(SIGIO , &sa, nullptr);
//    sigaction(SIGPOLL, &sa, nullptr);
//    sigaction(SIGPWR, &sa, nullptr);
//    sigaction(SIGSYS, &sa, nullptr);
//    sigaction(SIGUNUSED, &sa, nullptr);

    return static_cast<jboolean>(false);
}
}
