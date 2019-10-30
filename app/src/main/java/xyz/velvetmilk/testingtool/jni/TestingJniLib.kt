package xyz.velvetmilk.testingtool.jni

import xyz.velvetmilk.testingtool.models.CipherResult

class TestingJniLib {

    init {
        System.loadLibrary("testing")
    }

    external fun nativeString(): String
    external fun nativeByteArray(byteArray: ByteArray): String
    external fun nativeToByteArray(string: String): ByteArray
    external fun nativeGrabSha256(path: String): String
    external fun nativeTestDlSym(): Boolean

    external fun prepare()
    external fun addDigit(byte: Byte): Boolean
    external fun removeDigit(): Boolean
    external fun complete(): ByteArray

    external fun random(): Byte
    external fun urandom(): Byte

    external fun decryptKey(key: ByteArray, iv: ByteArray): ByteArray
    external fun encryptPin(key: ByteArray, data: ByteArray): CipherResult
}
