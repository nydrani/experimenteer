package xyz.velvetmilk.testingtool.jni

/**
 * @author Victor Zhang
 */
class AttestationJNILib {

    init {
        System.loadLibrary("attestation")
    }

    external fun nativeFileStat(): Boolean
}
