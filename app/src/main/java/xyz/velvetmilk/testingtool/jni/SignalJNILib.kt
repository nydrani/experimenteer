package xyz.velvetmilk.testingtool.jni

/**
 * @author Victor Zhang
 */
class SignalJNILib {

    init {
        System.loadLibrary("signal")
    }

    external fun setupSignalHandler(): Boolean
}
