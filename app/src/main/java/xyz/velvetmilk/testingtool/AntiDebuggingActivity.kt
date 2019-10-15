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
import xyz.velvetmilk.testingtool.jni.AntiDebuggingJniLib
import java.io.IOException
import java.util.zip.ZipFile
import kotlin.coroutines.CoroutineContext

class AntiDebuggingActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = AntiDebuggingActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, AntiDebuggingActivity::class.java)
        }
    }

    private val antiDebuggingJNILib = AntiDebuggingJniLib()

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

        try {
            val stringBuilder = StringBuilder()
            val zf = ZipFile(packageCodePath)
            for (entry in zf.entries()) {
                if (entry.name.contains(".so") || entry.name.contains(".dex")) {
                    stringBuilder.appendln(entry.name)
                }
            }
            base_view.text = stringBuilder.toString()
        } catch (e: IOException) {
            base_view.text = e.localizedMessage
        }

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()
            // NOTE: Run once per application
            stringBuilder.appendln(antiDebuggingJNILib.antiDebuggingPTrace())

            // NOTE: QEMU check never tested
            stringBuilder.appendln(String.format("QEMU check: %d", antiDebuggingJNILib.antiDebuggingQEMU()))

            base_view.text = stringBuilder.toString()
        }

        fab2.setOnClickListener {
            val stringBuilder = StringBuilder()
            stringBuilder.appendln(antiDebuggingJNILib.pthreadTest())

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
