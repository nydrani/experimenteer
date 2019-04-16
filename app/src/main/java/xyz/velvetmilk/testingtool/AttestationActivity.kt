package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.safetynet.SafetyNet
import com.google.gson.Gson
import com.scottyab.rootbeer.RootBeer
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_attestation.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

class AttestationActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = AttestationActivity::class.java.simpleName
        private const val apiKey = "***REMOVED***"


        fun buildIntent(context: Context): Intent {
            return Intent(context, AttestationActivity::class.java)
        }
    }

    private val attestationJNILib = AttestationJNILib()
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val disposer = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attestation)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()


        fab.setOnClickListener {
            launch(Dispatchers.IO) {
                val attestResult = attestSafetyNet().await()
                launch(Dispatchers.Main) {
                    log_view.text = attestResult
                }
            }

            launch(Dispatchers.IO) {
                val res = attestRootbeer()
                launch(Dispatchers.Main) {
                    log_view2.text = res.toString()
                }
            }

            launch(Dispatchers.IO) {
                val res = attestCustomMagiskFileStat()
                launch(Dispatchers.Main) {
                    log_view3.text = String.format("File stat: %b", res)
                }
            }

            launch(Dispatchers.IO) {
                val res = attestCustomMagiskUDS()
                launch(Dispatchers.Main) {
                    log_view4.text = String.format("UDS name check: %b", res)
                }
            }

            launch(Dispatchers.IO) {
                val res = attestCustomMagiskNativeFileStat()
                launch(Dispatchers.Main) {
                    log_view5.text = String.format("Native file stat: %b", res)
                }
            }
        }

        fab2.setOnClickListener {
            launch(Dispatchers.IO) {
                suExec("echo hi")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        disposer.clear()
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

    private fun attestSafetyNet(): Deferred<String> {
        val completableDeferred = CompletableDeferred<String>()
        SafetyNet.getClient(this).attest("hello".toByteArray(Charsets.UTF_8), apiKey)
            .addOnSuccessListener {
                val jwtParts = it.jwsResult.split(".")
                val decodedResult = Base64.decode(jwtParts[1], Base64.DEFAULT).toString(Charsets.UTF_8)
                val map = Gson().fromJson(decodedResult, Map::class.java)
                completableDeferred.complete(map.toString())
            }
            .addOnFailureListener {
                completableDeferred.completeExceptionally(it)
            }

        // do safetynet with output
        return completableDeferred
    }

    private fun attestRootbeer(): Map<String, Boolean> {
        val rootBeer = RootBeer(this)

        val map = mutableMapOf<String, Boolean>()
        map["Busybox"] = rootBeer.checkForBusyBoxBinary()
        map["Dangerous Props"] = rootBeer.checkForDangerousProps()
        map["Magisk"] = rootBeer.checkForMagiskBinary()
        map["Root native"] = rootBeer.checkForRootNative()
        map["Su binary"] = rootBeer.checkForSuBinary()
        map["Su exists"] = rootBeer.checkSuExists()
        map["Root cloaking"] = rootBeer.detectRootCloakingApps()
        map["Root management"] = rootBeer.detectRootManagementApps()
        map["Test keys"] = rootBeer.detectTestKeys()
        map["Dangerous apps"] = rootBeer.detectPotentiallyDangerousApps()
        map["RW paths"] = rootBeer.checkForRWPaths()

        return map
    }

    private fun attestCustomMagiskFileStat(): Boolean {
        val magiskFiles = listOf("/sbin/magiskinit",
            "/sbin/magisk",
            "/sbin/.magisk",
            "/sbin/.core",
            "/data/adb/magisk",
            "/data/adb/magisk.img",
            "/data/adb/magisk.db",
            "/cache/magisk.log")

        for (file in magiskFiles) {
            try {
                val stat = Os.stat(file)
                Timber.d(OsConstants.S_ISREG(stat.st_mode).toString())
            } catch (e: ErrnoException) {
                Timber.d(OsConstants.errnoName(e.errno))
            }
        }

        return false
    }

    // NOTE: Magisk v19.0 Uses custom UDS names of length 32 prepended by an @ symbol
    // regex expression should be [a-zA-Z0-9] (no spaces, no special characters)
    // e.g. @DFlakjl32slkfdjv23kjhfkjgho2vBDH
    private fun attestCustomMagiskUDS(): Boolean {
        BufferedReader(FileReader("/proc/net/unix")).use { reader ->
            val pattern = Pattern.compile("^.+: \\d+ \\d+ \\d+ \\d+ \\d+ \\d+ (@\\w{32})$")

            var line = reader.readLine()
            while (line != null) {
                val match = pattern.matcher(line)
                if (match.find()) {
                    Timber.d(match.group(1))
                    return true
                }
                line = reader.readLine()
            }
        }

        return false
    }

    private fun attestCustomMagiskNativeFileStat(): Boolean {
        return attestationJNILib.nativeFileStat()
    }

    private fun suExec(strCommand: String) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            Log.d("Exec.exec", "Executing command $strCommand")

            os.writeBytes(strCommand + "\n")
            os.flush()
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
