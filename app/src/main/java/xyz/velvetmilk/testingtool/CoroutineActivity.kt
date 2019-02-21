package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_coroutine.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CoroutineActivity : AppCompatActivity() {

    companion object {
        private val TAG = CoroutineActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, CoroutineActivity::class.java)
        }
    }

    @UseExperimental(kotlinx.coroutines.ObsoleteCoroutinesApi::class)
    private val singleThreadedContext = newSingleThreadContext("singleThreadBaby")

    private val disposer = CompositeDisposable()
    private var logBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fab.setOnClickListener {
            coroutineFun()
        }

        fab2.setOnClickListener {
            coroutineRunBlocking()
        }

        val byteBuffer = ByteBuffer.allocate(100)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(1)
        byteBuffer.put(2)
        byteBuffer.put(3)
        byteBuffer.put(4)

        val byteArraySize = 4

        Timber.d("arrayOffset: %d", byteBuffer.arrayOffset())
        Timber.d("byteArray: %s", pythonPrintByteArray(byteBuffer.array(), byteBuffer.arrayOffset(), byteArraySize))
    }

    override fun onDestroy() {
        super.onDestroy()

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


    private fun pythonPrintByteArray(array: ByteArray, offset: Int, size: Int): String {
        val stringBuilder = StringBuilder()
        for (i in offset until size+offset) {
            stringBuilder.append(array[i])
        }

        return stringBuilder.toString()
    }

    private fun coroutineFun() {
        GlobalScope.launch {
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
        GlobalScope.launch {
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
}
