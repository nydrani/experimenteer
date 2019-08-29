package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_barcode_scanner.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.dm7.barcodescanner.zxing.ZXingScannerView
import timber.log.Timber
import xyz.velvetmilk.testingtool.tools.PermissionsHelper
import kotlin.coroutines.CoroutineContext

class BarcodeScannerActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = BarcodeScannerActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, BarcodeScannerActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val handler = object : ZXingScannerView.ResultHandler {
        override fun handleResult(rawResult: com.google.zxing.Result?) {
            rawResult?.let {
                Timber.d(it.text)
                Timber.d(it.barcodeFormat.toString())
            }

            scanner_view.resumeCameraPreview(this)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        // Request permissions
        PermissionsHelper.requestPermissions(this, PermissionsHelper.cameraPermissions)

        scanner_view.setResultHandler(handler)

        fab.setOnClickListener {
            scanner_view.startCamera()
        }

        fab2.setOnClickListener {
            scanner_view.stopCamera()
        }
    }

    override fun onPause() {
        super.onPause()

        scanner_view.stopCamera()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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

            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}
