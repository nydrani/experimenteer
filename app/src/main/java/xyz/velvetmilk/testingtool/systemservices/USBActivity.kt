package xyz.velvetmilk.testingtool.systemservices

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_usb.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.R
import kotlin.coroutines.CoroutineContext

class USBActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = USBActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, USBActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usb)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()


        fab.setOnClickListener {
            val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val stringBuilder = StringBuilder()

            if (packageManager.hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY)) {
                stringBuilder.appendln(usbManager.accessoryList)
            }
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
                stringBuilder.appendln(usbManager.deviceList)
            }

            base_view.text = stringBuilder.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        disposer.clear()
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
}
