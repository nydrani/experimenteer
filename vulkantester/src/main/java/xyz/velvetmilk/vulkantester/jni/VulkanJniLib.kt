package xyz.velvetmilk.testingtool.jni

class VulkanJniLib {

    init {
        System.loadLibrary("vulkantesting")
    }

    external fun nativeString(): String
}
