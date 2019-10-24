package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.scottyab.rootbeer.RootBeer
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_attestation.*
import kotlinx.coroutines.*
import timber.log.Timber
import xyz.velvetmilk.testingtool.jni.AttestationJniLib
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.FileReader
import java.io.InputStreamReader
import java.util.stream.Collectors
import kotlin.coroutines.CoroutineContext

class AttestationActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = AttestationActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, AttestationActivity::class.java)
        }
    }

    private val attestationJNILib = AttestationJniLib()

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attestation)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            val builder = StringBuilder()

            builder.appendln(String.format("Rootbeer: %s", attestRootbeer()))
            builder.appendln(String.format("File stat: %b", attestCustomMagiskFileStat()))
            builder.appendln(String.format("System mounts: %b", attestCustomSystemMounts()))
            builder.appendln(String.format("Native file stat: %b", attestNativeFileStat()))
//            builder.appendln(String.format("Native change directory: %b", attestNativeChangeDirectory()))
//            builder.appendln(String.format("Native open directory: %b", attestNativeOpenDirectory()))
            builder.appendln(String.format("Native access directory: %b", attestNativeAccessDirectory()))
            builder.appendln(String.format("Native lstat directory: %b", attestNativeLStatDirectory()))
//            builder.appendln(String.format("Native get environ variables: %b", attestNativeGetEnvironVariables()))
            builder.appendln(String.format("Native check memory map: %b", attestNativeCheckMemoryMap()))
            builder.appendln(String.format("Native call popen: %b", attestNativeCallPopen()))
//            builder.appendln(String.format("Native call dmesg: %b", attestNativeCallDmesg()))
//            builder.appendln(String.format("Native call system sh: %b", attestNativeCallSystemSh()))
//            builder.appendln(String.format("Native call ps -A: %b", attestNativeCallProcessList()))
//            builder.appendln(String.format("Native call fork: %b", attestNativeCallFork()))
            builder.appendln(String.format("Native call cpuInfo: %b", attestNativeCpuInfo()))
//            builder.appendln(String.format("Native open proc directory: %b", attestNativeOpenProcDirectory()))
//            builder.appendln(String.format("Native make directory: %b", attestNativeMakeDirectory()))
            builder.appendln(String.format("Native check system properties: %b", attestNativeCheckSystemProperties()))
            builder.appendln(String.format("Native collect system properties: %s", attestNativeCollectSystemProperties().toString()))

            launch(Dispatchers.Main) {
                log_view.text = builder.toString()
            }
        }

        fab2.setOnClickListener {
            launch(Dispatchers.IO) {
                //suExec("echo hi")
                //shExec("hi")
                shExecProcess("hi")
            }
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
                Timber.d(String.format("File stat found: %s", OsConstants.S_ISREG(stat.st_mode)))
                return true
            } catch (e: ErrnoException) {
                Timber.e(String.format("File stat error: %s", OsConstants.errnoName(e.errno)))
            }
        }

        return false
    }

    private fun attestCustomSystemMounts(): Boolean {
        BufferedReader(FileReader("/proc/mounts")).use { reader ->
            return reader.lines().parallel().collect(Collectors.joining()).contains("magisk", true)
        }
    }

    private fun attestNativeFileStat(): Boolean {
        return attestationJNILib.nativeFileStat()
    }

    private fun attestNativeChangeDirectory(): Boolean {
        return attestationJNILib.changeDirectory()
    }

    private fun attestNativeOpenDirectory(): Boolean {
        return attestationJNILib.openDirectory()
    }

    private fun attestNativeAccessDirectory(): Boolean {
        return attestationJNILib.accessDirectory()
    }

    private fun attestNativeLStatDirectory(): Boolean {
        return attestationJNILib.lstatDirectory()
    }

    private fun attestNativeCheckMemoryMap(): Boolean {
        return attestationJNILib.checkMemoryMap()
    }

    private fun attestNativeGetEnvironVariables(): Boolean {
        return attestationJNILib.getEnvironVariables()
    }

    private fun attestNativeOpenProcDirectory(): Boolean {
        return attestationJNILib.openProcDirectory()
    }

    private fun attestNativeCallPopen(): Boolean {
        return attestationJNILib.callPopen()
    }

    private fun attestNativeCallDmesg(): Boolean {
        return attestationJNILib.callDmesg()
    }

    private fun attestNativeCallSystemSh(): Boolean {
        return attestationJNILib.callSystemSh()
    }

    private fun attestNativeCallProcessList(): Boolean {
        return attestationJNILib.callProcessList()
    }

    private fun attestNativeCallFork(): Boolean {
        return attestationJNILib.callFork()
    }

    private fun attestNativeCpuInfo(): Boolean {
        return attestationJNILib.cpuInfo()
    }

    private fun attestNativeMakeDirectory(): Boolean {
        return attestationJNILib.makeDirectory()
    }

    private fun attestNativeCheckSystemProperties(): Boolean {
        return attestationJNILib.checkSystemProperties()
    }

    private fun attestNativeCollectSystemProperties(): Map<String, String> {
        return attestationJNILib.collectSystemProperties()
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

    private fun shExec(strCommand: String) {
        try {
            val process = Runtime.getRuntime().exec("sh -c 'whoami'")
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

    private fun shExecProcess(strCommand: String) {
        try {
            val processBuilder = ProcessBuilder("sh", "-c", "whoami")
            val process = processBuilder.start()

            val stream = BufferedReader(InputStreamReader(process.inputStream))
            process.waitFor()
            Timber.d("Executed: " + stream.lines().parallel().collect(Collectors.joining()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
