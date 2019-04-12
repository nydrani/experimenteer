package xyz.velvetmilk.testingtool

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_opensles.*
import timber.log.Timber
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder


@RuntimePermissions
class OpenSLESActivity : AppCompatActivity() {

    companion object {
        fun buildIntent(context: Context): Intent {
            return Intent(context, OpenSLESActivity::class.java)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opensles)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup OpenSLES
        EavesJNILib.createEngine()
        EavesJNILib.createAudioRecorder()
        EavesJNILib.createAudioPlayer()

        // Setup click listeners
        start_recording_button.setOnClickListener {
            Timber.d("start recording")

            startRecordingWithPermissionCheck()
        }

        stop_recording_button.setOnClickListener {
            Timber.d("stop receive")

            stopRecordingWithPermissionCheck()
        }

        store_button.setOnClickListener {
            Timber.d("store recording")

            storeRecordingWithPermissionCheck()
        }

        play_button.setOnClickListener {
            Timber.d("play button")

            playRecording()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        EavesJNILib.shutdown()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        EavesJNILib.startRecording()
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun stopRecording() {
        EavesJNILib.stopRecording()
    }

    private fun playRecording() {
        EavesJNILib.startPlaying()
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun storeRecording() {
        // obtain pcm bytearray data from ndk
        val pcmBytes: ByteArray = EavesJNILib.getRecordingData()

        val sampleDir = File(Environment.getExternalStorageDirectory(), "/OpenSLESRecording")
        if (!sampleDir.exists()) {
            sampleDir.mkdirs()
            return
        }

        // Write to file
        val audioFile: File
        try {
            audioFile = File.createTempFile("Record", ".wav", sampleDir)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        // write pcm bytes into byteoutstream
        val byteOutStream = ByteArrayOutputStream()
        byteOutStream.write(pcmBytes)

        // file to store wav into
        val fos = FileOutputStream(audioFile)
        val bos = BufferedOutputStream(fos)
        val outStream = DataOutputStream(bos)

        writeWAV(byteOutStream, outStream)
        Timber.d("wrote to file: %s", audioFile.absolutePath)

        byteOutStream.close()
        outStream.close()
    }

    private fun writeWAV(dataStream: ByteArrayOutputStream, outStream: DataOutputStream) {
        val sampleRate = 44100
        val bitsPerSample: Short = 16
        val channelSize: Short = 2

        val headerSize = 44
        val headerSizeAfterSig = headerSize - 8
        val formatType: Short = 1
        val byteRate = sampleRate * bitsPerSample * channelSize / 8
        val blockAlign: Short = (bitsPerSample * channelSize / 8).toShort()

        val byteBuffer = ByteBuffer.allocateDirect(64)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        byteBuffer.put('R'.toByte())
        byteBuffer.put('I'.toByte())
        byteBuffer.put('F'.toByte())
        byteBuffer.put('F'.toByte())
        byteBuffer.putInt(dataStream.size() + headerSizeAfterSig)
        byteBuffer.put('W'.toByte())
        byteBuffer.put('A'.toByte())
        byteBuffer.put('V'.toByte())
        byteBuffer.put('E'.toByte())
        byteBuffer.put('f'.toByte())
        byteBuffer.put('m'.toByte())
        byteBuffer.put('t'.toByte())
        byteBuffer.put(' '.toByte())
        byteBuffer.putInt(16)
        byteBuffer.putShort(formatType)
        byteBuffer.putShort(channelSize)
        byteBuffer.putInt(sampleRate)
        byteBuffer.putInt(byteRate)
        byteBuffer.putShort(blockAlign)
        byteBuffer.putShort(bitsPerSample)
        byteBuffer.put('d'.toByte())
        byteBuffer.put('a'.toByte())
        byteBuffer.put('t'.toByte())
        byteBuffer.put('a'.toByte())
        byteBuffer.putInt(dataStream.size())

        Timber.d("size: %d", dataStream.size())
        Timber.d("offset: %d", byteBuffer.arrayOffset())
        Timber.d("array: %s", byteBuffer.array().toRawString(headerSize, byteBuffer.arrayOffset()))

        // NOTE: NEED TO OFFSET THIS ARRAY BY THE BYTEBUFFER OFFSET, NOT SURE WHY THERES AN OFFSET
        outStream.write(byteBuffer.array(), byteBuffer.arrayOffset(), headerSize)
        dataStream.writeTo(outStream)
    }
}
