package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_sensor.*
import kotlinx.coroutines.*
import timber.log.Timber
import xyz.velvetmilk.testingtool.views.SensorAdapter
import kotlin.coroutines.CoroutineContext

class SensorActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SensorActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, SensorActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var started = false

    private lateinit var adapter: SensorAdapter
    private lateinit var sensorMap: MutableMap<String, FloatArray>

    private lateinit var sensorManager: SensorManager
    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
//            Timber.d("onAccuracyChanged")
//            Timber.d(accuracy.toString())
        }

        override fun onSensorChanged(event: SensorEvent) {
//            Timber.d("onSensorChanged")

            sensorMap[event.sensor.name] = event.values
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        sensorMap = mutableMapOf()

        adapter = SensorAdapter()
//        adapter.setHasStableIds(true)
        adapter.viewClickSubject.subscribe {
            Timber.d(it.second.toString())
        }.addTo(disposer)

        sensor_recycler_view.adapter = adapter
        sensor_recycler_view.layoutManager = LinearLayoutManager(this)
        sensor_recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        sensor_recycler_view.isNestedScrollingEnabled = false
        (sensor_recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

        val stringBuilder = StringBuilder()
//        stringBuilder.appendln(sensorManager.isDynamicSensorDiscoverySupported)
//        stringBuilder.appendln()

        // display base sensor info
        for (sensor in deviceSensors) {
            if (sensor.isWakeUpSensor) {
                // dont watch wakeup sensors
                continue
            }
//            stringBuilder.appendln(sensor.id)
//            stringBuilder.appendln(sensor.maxDelay)
//            stringBuilder.appendln(sensor.maximumRange)
//            stringBuilder.appendln(sensor.minDelay)
            stringBuilder.appendln(sensor.name)
//            stringBuilder.appendln(sensor.power)
//            stringBuilder.appendln(sensor.reportingMode)
//            stringBuilder.appendln(sensor.resolution)
//            stringBuilder.appendln(sensor.stringType)
//            stringBuilder.appendln(sensor.type)
//            stringBuilder.appendln(sensor.vendor)
//            stringBuilder.appendln(sensor.version)
//            stringBuilder.appendln(sensor.isAdditionalInfoSupported)
//            stringBuilder.appendln(sensor.isDynamicSensor)
//            stringBuilder.appendln(sensor.isWakeUpSensor)
//            stringBuilder.appendln()

            sensorMap[sensor.name] = floatArrayOf()
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        base_view.text = stringBuilder.toString()

        // update at 60hz
        fab.setOnClickListener {
            if (started) {
                return@setOnClickListener
            }
            started = true

            launch {
                val innerStringBuilder = StringBuilder()

                while (true) {
                    innerStringBuilder.clear()
                    sensorMap.forEach {
                        innerStringBuilder.appendln(it.key)
                        innerStringBuilder.appendln("[")
                        for (value in it.value) {
                            innerStringBuilder.append("  ")
                            innerStringBuilder.appendln(value)
                        }
                        innerStringBuilder.appendln("]")
                    }
                    sensor_view.text = innerStringBuilder.toString()
                    delay(1000L / 60)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in deviceSensors) {
            if (sensor.isWakeUpSensor) {
                // dont watch wakeup sensors
                continue
            }
            sensorManager.unregisterListener(sensorEventListener)
        }

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
