package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_coroutine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class CoroutineActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = CoroutineActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, CoroutineActivity::class.java)
        }
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @UseExperimental(kotlinx.coroutines.ObsoleteCoroutinesApi::class)
    private val singleThreadedContext = newSingleThreadContext("singleThreadBaby")

    private val channel: Channel<String> = Channel()
    private val subject: Subject<String> = PublishSubject.create()

    private val disposer = CompositeDisposable()
    private var logBuilder = StringBuilder()
    private var debounceBuilder = StringBuilder()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()

        fab.setOnClickListener {
            coroutineFun()
        }

        fab2.setOnClickListener {
            coroutineRunBlocking()
        }

        fab3.setOnClickListener {
            click()
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
            @UseExperimental(kotlinx.coroutines.ObsoleteCoroutinesApi::class)
            channel.debounce(1000)
                .consumeEach {
                    launch(Dispatchers.Main) {
                        log_view.text = it
                    }
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

        val byteBuffer = ByteBuffer.allocate(100)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(1)
        byteBuffer.put(2)
        byteBuffer.put(3)
        byteBuffer.put(4)

        val byteArraySize = 4

        Timber.d("arrayOffset: %d", byteBuffer.arrayOffset())
        Timber.d("byteArray: %s", byteBuffer.array().toRawString(byteArraySize, byteBuffer.arrayOffset()))
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()

        singleThreadedContext.cancel()
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
            Thread.sleep(600)
            logBuilder.appendln("after slept")

            launch(Dispatchers.Main) {
                log_view.text = logBuilder.toString()
            }
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

            launch(Dispatchers.Main) {
                log_view.text = logBuilder.toString()
            }
        }
    }

    private fun click() {
        debounceBuilder.append("x")

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
