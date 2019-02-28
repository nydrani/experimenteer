package xyz.velvetmilk.testingtool

/**
 * @author Victor Zhang
 */
class EavesJNILib {

    companion object {
        init {
            System.loadLibrary("eaves")
        }

        @JvmStatic
        external fun createEngine()

        @JvmStatic
        external fun createAudioRecorder(): Boolean

        @JvmStatic
        external fun createAudioPlayer(): Boolean

        @JvmStatic
        external fun startPlaying()

        @JvmStatic
        external fun startRecording()

        @JvmStatic
        external fun stopRecording()

        @JvmStatic
        external fun getRecordingData(): ByteArray

        @JvmStatic
        external fun shutdown()
    }
}
