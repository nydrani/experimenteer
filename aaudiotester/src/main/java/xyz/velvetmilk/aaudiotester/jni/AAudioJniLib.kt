package xyz.velvetmilk.aaudiotester.jni

class AAudioJniLib {

    init {
        System.loadLibrary("testaaudio")
    }

    external fun createEngine()
    external fun shutdown()

    external fun getState()
}
