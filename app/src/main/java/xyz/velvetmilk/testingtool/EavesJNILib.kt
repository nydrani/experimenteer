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
        external fun createBufferQueueAudioPlayer(sampleRate: Int, samplesPerBuf: Int): Boolean

        @JvmStatic
        external fun createAudioRecorder(): Boolean

        @JvmStatic
        external fun startRecording()

        @JvmStatic
        external fun stopRecording()

        @JvmStatic
        external fun getRecordingData(): ByteArray

        @JvmStatic
        external fun shutdown()

        @JvmStatic
        external fun nativeByteArray(byteArray: ByteArray): String

        @JvmStatic
        external fun nativeToByteArray(string: String): ByteArray
    }
}
