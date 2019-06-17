package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_transition.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


class TransitionActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = TransitionActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, TransitionActivity::class.java)
        }
    }


    private var transitioned = false

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            TransitionManager.beginDelayedTransition(transition_container)
            transitioned = !transitioned
            if (transitioned) {
                transition_text.visibility = View.GONE
                transition_content.gravity = Gravity.CENTER_HORIZONTAL
            } else {
                transition_text.visibility = View.VISIBLE
                transition_content.gravity = Gravity.CENTER
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        disposer.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
