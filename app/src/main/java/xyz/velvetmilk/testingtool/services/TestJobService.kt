package xyz.velvetmilk.testingtool.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import kotlinx.coroutines.*
import timber.log.Timber
import xyz.velvetmilk.testingtool.systemservices.JobSchedulerActivity
import kotlin.coroutines.CoroutineContext

class TestJobService : JobService(), CoroutineScope {

    companion object {
        private val TAG = TestJobService::class.simpleName
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job


    // logging tools
    override fun onCreate() {
        Timber.tag(TAG)
        Timber.d("onCreate")

        super.onCreate()

        job = Job()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(TAG)
        Timber.d("onStartCommand")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        Timber.tag(TAG)
        Timber.d("onDestroy")

        job.cancel()
    }

    // LETS DO BACKGROUND STUFF
    override fun onStartJob(params: JobParameters): Boolean {
        launch {
            // do something
            val extraString = params.extras.getString(JobSchedulerActivity.EXTRA_JOB_STRING) ?: ""
            val jobId = params.jobId

            Timber.tag(TAG)
            Timber.d(String.format("jobId: %d | %s", jobId, extraString))

            // delay for one minute
            delay(60000)

            Timber.tag(TAG)
            Timber.d(String.format("jobId: %d complete", jobId))

            jobFinished(params, false)
        }

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }
}
