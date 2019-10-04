package xyz.velvetmilk.testingtool.systemservices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.health.SystemHealthManager
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_system_health.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.R
import kotlin.coroutines.CoroutineContext

class SystemHealthActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SystemHealthActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, SystemHealthActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_health)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            val systemHealthManager = getSystemService(Context.SYSTEM_HEALTH_SERVICE) as SystemHealthManager
            val stringBuilder = StringBuilder()

            val uidStats = systemHealthManager.takeMyUidSnapshot()
            stringBuilder.appendln(uidStats.dataType)

            stringBuilder.appendln(uidStats.measurementKeyCount)
            for (index in 0 until uidStats.measurementKeyCount) {
                val key = uidStats.getMeasurementKeyAt(index)
                val measurement = uidStats.getMeasurement(key)
                stringBuilder.appendln(String.format("Key: %d | Value: %d", key, measurement))
            }

            stringBuilder.appendln(uidStats.measurementsKeyCount)
            for (index in 0 until uidStats.measurementsKeyCount) {
                val key = uidStats.getMeasurementsKeyAt(index)
                val measurements = uidStats.getMeasurements(key)
                stringBuilder.appendln(String.format("Key: %d | Value: %s", key, measurements))
            }

            stringBuilder.appendln(uidStats.statsKeyCount)
            for (index in 0 until uidStats.statsKeyCount) {
                val key = uidStats.getStatsKeyAt(index)
                val stats = uidStats.getStats(key)
                stringBuilder.appendln(String.format("Key: %d | Value: %s", key, stats))
            }

            stringBuilder.appendln(uidStats.timerKeyCount)
            for (index in 0 until uidStats.timerKeyCount) {
                val key = uidStats.getTimerKeyAt(index)
                val timer = uidStats.getTimer(key)
                stringBuilder.appendln(String.format("Key: %d | count: %d, time: %d", key, timer.count, timer.time))
            }

            stringBuilder.appendln(uidStats.timersKeyCount)
            for (index in 0 until uidStats.timersKeyCount) {
                val key = uidStats.getTimersKeyAt(index)
                val timers = uidStats.getTimers(key)
                stringBuilder.appendln(String.format("Key: %d | Value: %s", key, timers))
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
