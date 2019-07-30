package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_opensles.*
import timber.log.Timber
import xyz.velvetmilk.testingtool.jni.EavesJniLib
import xyz.velvetmilk.testingtool.tools.PermissionsHelper
import xyz.velvetmilk.testingtool.tools.toRawString
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OpenSlesActivity : AppCompatActivity() {

    companion object {
        fun buildIntent(context: Context): Intent {
            return Intent(context, OpenSlesActivity::class.java)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opensles)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Need audio permissions for this screen
        if (PermissionsHelper.checkAndRequestPermissions(this, PermissionsHelper.openSlesPermissions)) {
            initEavesEngine()
        }

        // Setup click listeners
        start_recording_button.setOnClickListener {
            Timber.d("start recording")

            startRecording()
        }

        stop_recording_button.setOnClickListener {
            Timber.d("stop receive")

            stopRecording()
        }

        store_button.setOnClickListener {
            Timber.d("store recording")

            storeRecording()
        }

        play_button.setOnClickListener {
            Timber.d("play button")

            playRecording()
        }

        // Some random byte buffer test code
        val byteBuffer = ByteBuffer.allocate(100)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(1)
        byteBuffer.put(2)
        byteBuffer.put(3)
        byteBuffer.put(4)

        val byteArraySize = 4

        Timber.d("arrayOffset: %d", byteBuffer.arrayOffset())
        Timber.d("byteArray: %s", byteBuffer.array().toRawString(byteArraySize, byteBuffer.arrayOffset()))
    }

    override fun onDestroy() {
        super.onDestroy()

        EavesJniLib.shutdown()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            0 -> {
                // if nothing granted
                if (grantResults.isEmpty()) {
                    finish()
                    return
                }

                // if not everything granted
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        finish()
                        return
                    }
                }

                // what to do
                initEavesEngine()
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun initEavesEngine() {
        // Setup OpenSLES
        EavesJniLib.createEngine()
        EavesJniLib.createAudioRecorder()
        EavesJniLib.createAudioPlayer()
    }

    private fun startRecording() {
        EavesJniLib.startRecording()
    }

    private fun stopRecording() {
        EavesJniLib.stopRecording()
    }

    private fun playRecording() {
        EavesJniLib.startPlaying()
    }

    private fun storeRecording() {
        // obtain pcm bytearray data from ndk
        val pcmBytes: ByteArray = EavesJniLib.getRecordingData()

        val sampleDir = getExternalFilesDir(null)

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
