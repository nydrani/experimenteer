package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_system.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.jni.ExternalJniLib
import kotlin.coroutines.CoroutineContext

class SystemActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SystemActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, SystemActivity::class.java)
        }
    }

    private val externalJniLib = ExternalJniLib()

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(System.currentTimeMillis())
            stringBuilder.appendln(System.getProperties())
            stringBuilder.appendln(System.getenv())
            stringBuilder.appendln(System.getSecurityManager())
            stringBuilder.appendln(System.nanoTime())

            base_view.text = stringBuilder.toString()
        }

        fab2.setOnClickListener {
            base_view.text = externalJniLib.ping().toString()
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
