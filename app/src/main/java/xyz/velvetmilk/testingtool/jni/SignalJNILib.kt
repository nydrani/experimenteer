package xyz.velvetmilk.testingtool.jni

/**
 * @author Victor Zhang
 */
class SignalJNILib {

    init {
        System.loadLibrary("signal")
    }

    fun load() {}
    external fun setupSignalHandler(): Boolean
}
