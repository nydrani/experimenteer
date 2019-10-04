package xyz.velvetmilk.testingtool.systemservices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.HardwarePropertiesManager
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_hardware_properties.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.R
import kotlin.coroutines.CoroutineContext

class HardwarePropertiesActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = HardwarePropertiesActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, HardwarePropertiesActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hardware_properties)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        val deviceTypeList = listOf(HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN)

        val deviceSourceList = listOf(HardwarePropertiesManager.TEMPERATURE_CURRENT,
            HardwarePropertiesManager.TEMPERATURE_SHUTDOWN,
            HardwarePropertiesManager.TEMPERATURE_THROTTLING,
            HardwarePropertiesManager.TEMPERATURE_THROTTLING_BELOW_VR_MIN)

        fab.setOnClickListener {
            val hardwarePropertiesManager = getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as HardwarePropertiesManager
            val stringBuilder = StringBuilder()

            try {
                stringBuilder.appendln(hardwarePropertiesManager.cpuUsages)
                stringBuilder.appendln(hardwarePropertiesManager.fanSpeeds)

                for (type in deviceTypeList) {
                    for (source in deviceSourceList) {
                        stringBuilder.appendln(
                            hardwarePropertiesManager.getDeviceTemperatures(
                                type,
                                source
                            )
                        )
                    }
                }
            } catch (e: SecurityException) {
                stringBuilder.appendln("No android.permission.DEVICE_POWER permission")
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
