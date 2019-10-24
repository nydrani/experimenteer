package xyz.velvetmilk.testingtool.jni

class ExternalJniLib {

    init {
        System.loadLibrary("external")
    }

    external fun ping(): Boolean
}
