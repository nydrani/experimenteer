package xyz.velvetmilk.testingtool.jni

/**
 * @author Victor Zhang
 */
class SignalJniLib {

    init {
        System.loadLibrary("signal")
    }

    external fun setupSignalHandler(): Boolean
}
