package xyz.velvetmilk.testingtool

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_quiet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.quietmodem.Quiet.*
import timber.log.Timber
import java.io.IOException
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions


@RuntimePermissions
class QuietActivity : AppCompatActivity() {

    companion object {
        fun buildIntent(context: Context): Intent {
            return Intent(context, QuietActivity::class.java)
        }
    }

    private var receiver: LoopbackFrameReceiver? = null
    private var transmitter: LoopbackFrameTransmitter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiet)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        send_button.setOnClickListener {
            // when send -- start sending
            var data: String? = edit_input.text?.toString()
            if (data.isNullOrBlank()) {
                data = "??"
            }

            Timber.d("starting send: %s", data)

            sendQuietDataWithPermissionCheck(data)
        }

        receive_button.setOnClickListener {
            Timber.d("starting receive")

            receiveQuietDataWithPermissionCheck()
        }

        try {
            val defaultConfigs = FrameTransmitterConfig.getDefaultProfiles(this)
            Timber.d(defaultConfigs)
            val transmitterConfig = FrameTransmitterConfig(
                this,
                "ultrasonic"
            )
            val receiverConfig = FrameReceiverConfig(
                this,
                "ultrasonic"
            )

            receiver = LoopbackFrameReceiver(receiverConfig)
            transmitter = LoopbackFrameTransmitter(transmitterConfig)
        } catch (e: IOException) {
            // could not build configs
        } catch (e: ModemException) {
            // could not set up receiver/transmitter
        }

    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun sendQuietData(payload: String) {
        // send this data
        try {
            transmitter?.send(payload.toByteArray())
        } catch (e: IOException) {
            // our message might be too long or the transmit queue full
            Timber.e(e, "error when sending")
        }
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun receiveQuietData() {
        GlobalScope.launch {
            // set receiver to block until a frame is received
            // by default receivers are nonblocking
            receiver?.setBlocking(10, 0)

            val buf = ByteArray(1024)
            var recvLen: Long = 0
            try {
                receiver?.receive(buf)?.let {
                    recvLen = it
                }
                Timber.d("got data: %d", recvLen)

                launch(Dispatchers.Main) {
                    textView1.text = recvLen.toString() + " " + buf.toHexStringUTF8() + " " + buf.sliceArray(0 until recvLen.toInt()).contentToString()
                }
            } catch (e: IOException) {
                Timber.e(e, "error when receiving")
                launch(Dispatchers.Main) {
                    textView1.text = "got nothing"
                }
            }
        }
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
}
