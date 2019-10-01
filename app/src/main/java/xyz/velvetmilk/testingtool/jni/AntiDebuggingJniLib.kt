package xyz.velvetmilk.testingtool.jni

/**
 * @author Victor Zhang
 */
class AntiDebuggingJniLib {

    init {
        System.loadLibrary("antidebugging")
    }

    external fun pthreadTest(): Boolean
    external fun antiDebuggingPTrace(): Boolean
    external fun antiDebuggingQEMU(): Int
}
