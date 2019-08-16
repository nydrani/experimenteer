package xyz.velvetmilk.testingtool.jni

class TestingJniLib {

    init {
        System.loadLibrary("testing")
    }

    external fun nativeString(): String

    external fun nativeByteArray(byteArray: ByteArray): String

    external fun nativeToByteArray(string: String): ByteArray

    external fun nativeGrabSha256(path: String): String

    external fun nativeTestDlSym(path: String): Boolean
}
