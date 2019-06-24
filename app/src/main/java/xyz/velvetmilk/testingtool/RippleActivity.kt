package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_ripple.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class RippleActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = RippleActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, RippleActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ripple)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        ripple_view.isClickable = true

        fab.setOnClickListener {
            launch {
                ripple_view.background.state = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
                delay(100)
                ripple_view.background.state = intArrayOf()
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
