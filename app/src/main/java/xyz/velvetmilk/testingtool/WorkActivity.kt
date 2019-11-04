package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class WorkActivity : AppCompatActivity(), CoroutineScope {

    class UploadLogWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

        companion object {
            const val COUNT_DATA_KEY = "COUNT"
            const val TOTAL_COUNT_DATA_KEY = "TOTAL_COUNT"
        }

        // NOTE: this resets after every  count
        private var count = 0
        private var totalCount = 0

        override suspend fun doWork(): Result {
            val success = uploadLog()
            val countData = Data.Builder()
                .putInt(COUNT_DATA_KEY, count)
                .putInt(TOTAL_COUNT_DATA_KEY, totalCount)
                .build()

            return if (success) {
                Result.success(countData)
            } else {
                Result.retry()
            }
        }

        private fun uploadLog(): Boolean {
            // heh
            val rand = Random.nextBoolean()
            if (rand) {
                count++
            }
            totalCount++

            Timber.d(count.toString())
            Timber.d(rand.toString())

            return rand
        }
    }

    companion object {
        private val TAG = WorkActivity::class.simpleName
        private const val WORK_UUID_KEY = "WORK_UUID_KEY"

        fun buildIntent(context: Context): Intent {
            return Intent(context, WorkActivity::class.java)
        }
    }

    private var id: UUID? = null

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        val workManager = WorkManager.getInstance(this)
        val sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)

        // load uuid from sharedpreferences
        sharedPreferences.getString(WORK_UUID_KEY, null)?.let {
            id = UUID.fromString(it)
        }

        fab.setOnClickListener {
            if (id != null) {
                return@setOnClickListener
            }

            val uploadLogRequest = PeriodicWorkRequestBuilder<UploadLogWorker>(15, TimeUnit.MINUTES).build()

            // store uuid in sharedpreferences
            id = uploadLogRequest.id
            sharedPreferences.edit().putString(WORK_UUID_KEY, id.toString()).apply()

            workManager.enqueue(uploadLogRequest)
        }

        fab2.setOnClickListener {
            if (id == null) {
                return@setOnClickListener
            }

            workManager.cancelWorkById(id!!)

            // remove uuid from sharedpreferences
            sharedPreferences.edit().remove(WORK_UUID_KEY).apply()
            id = null
        }

        fab3.setOnClickListener {
            if (id == null) {
                return@setOnClickListener
            }

            launch(Dispatchers.Default) {
                val stringBuilder = StringBuilder()
                val info = workManager.getWorkInfoById(id!!).await()

                val count = info.outputData.getInt(UploadLogWorker.COUNT_DATA_KEY, 0)
                val totalCount = info.outputData.getInt(UploadLogWorker.TOTAL_COUNT_DATA_KEY, 0)

                stringBuilder.append("Count: ")
                stringBuilder.appendln(count)
                stringBuilder.append("Total Count: ")
                stringBuilder.appendln(totalCount)

                launch(Dispatchers.Main) {
                    base_view.text = stringBuilder.toString()
                }
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
}
