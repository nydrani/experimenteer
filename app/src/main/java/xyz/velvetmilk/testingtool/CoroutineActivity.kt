package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_coroutine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class CoroutineActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = CoroutineActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, CoroutineActivity::class.java)
        }
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @UseExperimental(kotlinx.coroutines.ObsoleteCoroutinesApi::class)
    private val singleThreadedContext = newSingleThreadContext("singleThreadBaby")

    @UseExperimental(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val broadcastChannel: BroadcastChannel<String> = BroadcastChannel(Channel.CONFLATED)
    private val channel: Channel<String> = Channel()
    private val subject: Subject<String> = PublishSubject.create()

    private val disposer = CompositeDisposable()
    private var logBuilder = StringBuilder()
    private var debounceBuilder = StringBuilder()

    private var curInstant = Instant.now()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()

        fab.setOnClickListener {
            Snackbar.make(it, "Coroutine run non-blocking", Snackbar.LENGTH_SHORT).show()
            coroutineFun()
        }

        fab2.setOnClickListener {
            Snackbar.make(it, "Coroutine run blocking", Snackbar.LENGTH_SHORT).show()
            coroutineRunBlocking()
        }

        fab3.setOnClickListener {
            click()
        }

        fab4.setOnClickListener {
            Snackbar.make(it, "Coroutine suspend", Snackbar.LENGTH_SHORT).show()
            launch {
                val builder = StringBuilder()
                for (i in 1..20) {
                    val s = doSomethingSuspend(i)

                    val newInstant = Instant.now()
                    val timeDiff = ChronoUnit.MILLIS.between(curInstant, newInstant)
                    curInstant = newInstant
                    builder.appendln(s)

                    log_view.text = builder.toString()
                    log_view3.text = timeDiff.toString()
                }
            }
        }

        fab5.setOnClickListener {
            Snackbar.make(it, "Coroutine async deferred", Snackbar.LENGTH_SHORT).show()
            launch {
                val builder = StringBuilder()
                for (i in 1..20) {
                    val s = doSomethingAsync(i).await()

                    val newInstant = Instant.now()
                    val timeDiff = ChronoUnit.MILLIS.between(curInstant, newInstant)
                    curInstant = newInstant
                    builder.appendln(s)

                    log_view2.text = builder.toString()
                    log_view3.text = timeDiff.toString()
                }
            }
        }

        fab6.setOnClickListener {
            Snackbar.make(it, "Spammer", Snackbar.LENGTH_SHORT).show()
            launch(Dispatchers.Default) {
                val curTime = Instant.now()
                val builder = StringBuilder()
                var res = Math.random()
                for (i in 1..10000000) {
                    res = Math.atan(res)
                    res = Math.tan(res)
                }
                builder.appendln(res)
                launch(Dispatchers.Main) {
                    log_view2.text = ChronoUnit.MILLIS.between(curTime, Instant.now()).toString()
                    log_view3.text = builder.toString()
                }
            }
        }

        Timber.d("onCreate thread: %s", Thread.currentThread().name)
        launch {
            Timber.d("launch thread: %s", Thread.currentThread().name)
            withContext(Dispatchers.IO) {
                Timber.d("withContext Dispatchers.IO thread: %s", Thread.currentThread().name)
            }
            withContext(Dispatchers.Main) {
                Timber.d("withContext Dispatchers.Main thread: %s", Thread.currentThread().name)
            }
            withContext(Dispatchers.Default) {
                Timber.d("withContext Dispatchers.Default thread: %s", Thread.currentThread().name)
            }
        }

        // load hot channel for debouncing clicks
        launch {
            val debounced = channel.debounce(1000)
            for (item in debounced) {
                log_view.text = item
            }
        }

        // using flow to debounce clicks
        launch {
            @UseExperimental(kotlinx.coroutines.FlowPreview::class)
            broadcastChannel.asFlow().debounce(1000).collect {
                log_view3.text = it
            }
        }

        // load hot?? or cold?? for debouncing clicks
        subject.subscribeOn(Schedulers.io())
            .debounce(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("Current thread %s", Thread.currentThread().name)
                log_view2.text = it
            }
            .addTo(disposer)
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()

        singleThreadedContext.cancel()
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


    private suspend fun doSomethingSuspend(index: Int): Int {
        return withContext(singleThreadedContext) {
            Thread.sleep(100)
            index
        }
    }

    private fun doSomethingAsync(index: Int): Deferred<Int> {
        val deferred = CompletableDeferred<Int>()
        launch(singleThreadedContext) {
            Thread.sleep(100)
            deferred.complete(index)
        }
        return deferred
    }

    private fun coroutineFun() {
        launch {
            logBuilder.appendln("start")

            for (i in 1..10) {
                launch(singleThreadedContext) {
                    logBuilder.appendln(i)
                    launch(Dispatchers.Main) {
                        log_view.text = logBuilder.toString()
                    }
                    Thread.sleep(100)
                }
            }

            logBuilder.appendln("after launched")
            delay(600)
            logBuilder.appendln("after delay")

            log_view.text = logBuilder.toString()
        }
    }

    private fun coroutineRunBlocking() {
        launch {
            logBuilder.appendln("start")

            for (i in 1..10) {
                withContext(singleThreadedContext) {
                    logBuilder.appendln(i)
                    launch(Dispatchers.Main) {
                        log_view.text = logBuilder.toString()
                    }
                    Thread.sleep(100)
                }
            }

            logBuilder.appendln("after single thread call")

            log_view.text = logBuilder.toString()
        }
    }

    private fun click() {
        debounceBuilder.append("x")

        @UseExperimental(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        broadcastChannel.offer(debounceBuilder.toString())
        channel.offer(debounceBuilder.toString())
        subject.onNext(debounceBuilder.toString())
    }

    @UseExperimental(kotlinx.coroutines.ObsoleteCoroutinesApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun <T> ReceiveChannel<T>.debounce(settleTime: Long): ReceiveChannel<T> {
        return produce {
            var job: Job? = null
            consumeEach {
                job?.cancel()
                job = launch {
                    delay(settleTime)
                    send(it)
                }
            }
            job?.join() //waiting for the last debouncing to end
        }
    }
}
