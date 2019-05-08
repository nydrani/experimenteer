package xyz.velvetmilk.testingtool


class TestingJNILib {

    init {
        System.loadLibrary("testing")
    }

    external fun nativeString(): String

    external fun nativeByteArray(byteArray: ByteArray): String

    external fun nativeToByteArray(string: String): ByteArray
}
