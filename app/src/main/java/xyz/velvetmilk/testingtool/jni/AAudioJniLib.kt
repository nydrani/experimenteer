package xyz.velvetmilk.testingtool.jni

class AAudioJniLib {

    init {
        System.loadLibrary("testaaudio")
    }

    external fun createEngine()
    external fun shutdown()
}
