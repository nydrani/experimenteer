package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Debug
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.threeten.bp.Instant
import xyz.velvetmilk.testingtool.tools.PermissionsHelper
import kotlin.coroutines.CoroutineContext

class InfoActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = InfoActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, InfoActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    @SuppressWarnings("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        // grab permission for serial read
        PermissionsHelper.requestPermissions(this, PermissionsHelper.infoPermissions)


        fab.setOnClickListener {
            val stringBuilder = StringBuilder()
            stringBuilder.appendln(String.format("BOARD: %s", android.os.Build.BOARD))
            stringBuilder.appendln(String.format("BOOTLOADER: %s", android.os.Build.BOOTLOADER))
            stringBuilder.appendln(String.format("BRAND: %s", android.os.Build.BRAND))
            stringBuilder.appendln(String.format("DEVICE: %s", android.os.Build.DEVICE))
            stringBuilder.appendln(String.format("DISPLAY: %s", android.os.Build.DISPLAY))
            stringBuilder.appendln(String.format("FINGERPRINT: %s", android.os.Build.FINGERPRINT))
            stringBuilder.appendln(String.format("HARDWARE: %s", android.os.Build.HARDWARE))
            stringBuilder.appendln(String.format("HOST: %s", android.os.Build.HOST))
            stringBuilder.appendln(String.format("ID: %s", android.os.Build.ID))
            stringBuilder.appendln(String.format("MANUFACTURER: %s", android.os.Build.MANUFACTURER))
            stringBuilder.appendln(String.format("MODEL: %s", android.os.Build.MODEL))
            stringBuilder.appendln(String.format("PRODUCT: %s", android.os.Build.PRODUCT))
            stringBuilder.appendln("===== SUPPORTED_32_BIT_ABIS =====")
            for (abi in android.os.Build.SUPPORTED_32_BIT_ABIS) {
                stringBuilder.appendln(String.format("32 BIT ABI: %s", abi))
            }
            stringBuilder.appendln("===== SUPPORTED_64_BIT_ABIS =====")
            for (abi in android.os.Build.SUPPORTED_64_BIT_ABIS) {
                stringBuilder.appendln(String.format("64 BIT ABI: %s", abi))
            }
            stringBuilder.appendln("===== SUPPORTED_ABIS =====")
            for (abi in android.os.Build.SUPPORTED_ABIS) {
                stringBuilder.appendln(String.format("ABI: %s", abi))
            }
            stringBuilder.appendln(String.format("TAGS: %s", android.os.Build.TAGS))
            stringBuilder.appendln(String.format("TIME: %s", Instant.ofEpochMilli(android.os.Build.TIME)))
            stringBuilder.appendln(String.format("TYPE: %s", android.os.Build.TYPE))
            stringBuilder.appendln(String.format("USER: %s", android.os.Build.USER))
            stringBuilder.appendln(String.format("RADIO VERSION: %s", android.os.Build.getRadioVersion()))

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                stringBuilder.appendln("===== Fingerprinted Parititons =====")
                for (partition in android.os.Build.getFingerprintedPartitions()) {
                    stringBuilder.appendln(
                        String.format(
                            "BUILDTIMEMILLIS: %s",
                            Instant.ofEpochMilli(partition.buildTimeMillis)
                        )
                    )
                    stringBuilder.appendln(String.format("FINGERPRINT: %s", partition.fingerprint))
                    stringBuilder.appendln(String.format("NAME: %s", partition.name))
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    stringBuilder.appendln(String.format("SERIAL: %s", android.os.Build.getSerial()))
                } catch (e: SecurityException) {
                    // whatever
                    stringBuilder.appendln("SERIAL READ FAILED")
                }
            } else {
                stringBuilder.appendln(String.format("SERIAL: %s", android.os.Build.SERIAL))
            }

            stringBuilder.appendln(String.format("ANDROID_ID: %s", Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)))
            stringBuilder.appendln()
            stringBuilder.appendln(String.format("VERSION BASE_OS: %s", android.os.Build.VERSION.BASE_OS))
            stringBuilder.appendln(String.format("VERSION CODENAME: %s", android.os.Build.VERSION.CODENAME))
            stringBuilder.appendln(String.format("VERSION INCREMENTAL: %s", android.os.Build.VERSION.INCREMENTAL))
            stringBuilder.appendln(String.format("VERSION PREVIEW_SDK_INT: %d", android.os.Build.VERSION.PREVIEW_SDK_INT))
            stringBuilder.appendln(String.format("VERSION RELEASE: %s", android.os.Build.VERSION.RELEASE))
            stringBuilder.appendln(String.format("VERSION SDK_INT: %d", android.os.Build.VERSION.SDK_INT))
            stringBuilder.appendln(String.format("VERSION SECURITY_PATCH: %s", android.os.Build.VERSION.SECURITY_PATCH))

            base_view.text = stringBuilder.toString()
        }

        fab2.setOnClickListener {
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(BuildConfig.DEBUG)
            stringBuilder.appendln((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
            stringBuilder.appendln(Debug.isDebuggerConnected())
            stringBuilder.appendln(detectDebuggingViaTiming())

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


    // TODO probably need to scatter this across places
    private fun detectDebuggingViaTiming(): Boolean {
        val start = Debug.threadCpuTimeNanos()

        for (i in 0..999999)
            continue

        val stop = Debug.threadCpuTimeNanos()

        return (stop - start > 10000000)
    }
}
