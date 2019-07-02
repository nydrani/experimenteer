package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_antidebugging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.jni.AntiDebuggingJNILib
import kotlin.coroutines.CoroutineContext

class AntiDebuggingActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = AntiDebuggingActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, AntiDebuggingActivity::class.java)
        }
    }

    private val antiDebuggingJNILib = AntiDebuggingJNILib()


    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antidebugging)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()
//            stringBuilder.appendln(antiDebuggingJNILib.antiDebuggingPTrace())
            stringBuilder.appendln(String.format("QEMU check: %d", antiDebuggingJNILib.antiDebuggingQEMU()))

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
