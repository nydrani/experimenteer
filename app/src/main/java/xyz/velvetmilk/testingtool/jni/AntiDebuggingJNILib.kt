package xyz.velvetmilk.testingtool.jni

/**
 * @author Victor Zhang
 */
class AntiDebuggingJNILib {

    init {
        System.loadLibrary("antidebugging")
    }

    external fun antiDebuggingPTrace(): Boolean
    external fun antiDebuggingQEMU(): Int
}
