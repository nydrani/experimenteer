package xyz.velvetmilk.testingtool

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_activity_manager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ActivityManagerActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = ActivityManagerActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, ActivityManagerActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_manager)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val stringBuilder = StringBuilder()

            // NOTE: static calls first
            val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
            ActivityManager.getMyMemoryState(runningAppProcessInfo)
            stringBuilder.appendln(runningAppProcessInfo)
            stringBuilder.appendln(ActivityManager.isUserAMonkey())
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                stringBuilder.appendln(ActivityManager.isRunningInUserTestHarness())
            } else {
                stringBuilder.appendln(ActivityManager.isRunningInTestHarness())
            }

            // converted calls
            stringBuilder.appendln(activityManager.appTaskThumbnailSize)
            stringBuilder.appendln(activityManager.appTasks)
            stringBuilder.appendln(activityManager.deviceConfigurationInfo)
            stringBuilder.appendln(activityManager.isLowRamDevice)
            stringBuilder.appendln(activityManager.largeMemoryClass)
            stringBuilder.appendln(activityManager.launcherLargeIconDensity)
            stringBuilder.appendln(activityManager.launcherLargeIconSize)
            stringBuilder.appendln(activityManager.lockTaskModeState)
            stringBuilder.appendln(activityManager.memoryClass)
            stringBuilder.appendln(activityManager.processesInErrorState)

            // other calls
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            stringBuilder.appendln(memoryInfo)

            // no idea what pids
            // stringBuilder.appendln(activityManager.getProcessMemoryInfo())

            // no idea what services
            // stringBuilder.appendln(activityManager.getRunningServiceControlPanel())

            stringBuilder.appendln("===== START ANDROID P =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                stringBuilder.appendln(activityManager.isBackgroundRestricted)
            }
            stringBuilder.appendln("===== END ANDROID P =====")

            stringBuilder.appendln("===== START ANDROID Q =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // seems like this for dual screen phones
                // stringBuilder.appendln(activityManager.isActivityStartAllowedOnDisplay())
            }
            stringBuilder.appendln("===== END ANDROID Q =====")


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
