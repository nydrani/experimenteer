package xyz.velvetmilk.testingtool

/**
 * @author Victor Zhang
 */
class AttestationJNILib {

    init {
        System.loadLibrary("attestation")
    }

    external fun nativeFileStat(): Boolean
}
