package xyz.velvetmilk.testingtool.jni

/**
 * @author Victor Zhang
 */
class AttestationJniLib {

    init {
        System.loadLibrary("attestation")
    }

    external fun nativeFileStat(): Boolean
    external fun changeDirectory(): Boolean
    external fun makeDirectory(): Boolean
    external fun openDirectory(): Boolean
    external fun accessDirectory(): Boolean
    external fun lstatDirectory(): Boolean
    external fun getEnvironVariables(): Boolean
    external fun checkMemoryMap(): Boolean
    external fun callPOpen(): Boolean
    external fun callDmesg(): Boolean
    external fun callSystemSh(): Boolean
    external fun callFork(): Boolean
    external fun callProcessList(): Boolean
    external fun openProcDirectory(): Boolean
    external fun checkSystemProperties(): Boolean
}
