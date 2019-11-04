package xyz.velvetmilk.testingtool.systemservices

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_job_scheduler.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.R
import xyz.velvetmilk.testingtool.services.TestJobService
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class JobSchedulerActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = JobSchedulerActivity::class.simpleName
        const val EXTRA_JOB_STRING = "EXTRA_JOB_STRING"
        const val EXTRA_IS_PERIODIC = "EXTRA_IS_PERIODIC"

        fun buildIntent(context: Context): Intent {
            return Intent(context, JobSchedulerActivity::class.java)
        }
    }

    private var jobId = 0
    private lateinit var jobScheduler: JobScheduler

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_scheduler)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()

            val id = Random.nextInt()
            val job = buildJob(id)
            if (jobScheduler.schedule(job) == JobScheduler.RESULT_SUCCESS) {
                jobId = id
            }

            stringBuilder.appendln(id)
            stringBuilder.appendln(jobId)

            base_view.text = stringBuilder.toString()
        }

        fab2.setOnClickListener {
            jobScheduler.cancel(jobId)
        }

        fab3.setOnClickListener {
            val stringBuilder = StringBuilder()

            val jobs = jobScheduler.allPendingJobs
            for (job in jobs) {
                stringBuilder.appendln(job.id)
            }

            jobScheduler.cancelAll()

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

    // NOTE: apparently must be 15 minutes +/- 5 minute flex
    private fun buildJob(jobId: Int): JobInfo {
        val extras = PersistableBundle()
        extras.putBoolean(EXTRA_IS_PERIODIC, true)
        extras.putString(EXTRA_JOB_STRING, "hello world")

        return JobInfo.Builder(jobId, ComponentName(this, TestJobService::class.java))
            .setExtras(extras)
            .setPeriodic(900000, 300000)
            .build()
    }
}
