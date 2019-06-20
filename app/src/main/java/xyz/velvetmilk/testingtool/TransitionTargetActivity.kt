package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.transition.addListener
import androidx.core.view.ViewCompat
import androidx.transition.TransitionManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_transition_target.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import xyz.velvetmilk.testingtool.tools.getRandomString
import kotlin.coroutines.CoroutineContext


class TransitionTargetActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = TransitionTargetActivity::class.simpleName
        const val TRANSITION_IMAGE = "transition:image"

        fun buildIntent(context: Context): Intent {
            return Intent(context, TransitionTargetActivity::class.java)
        }
    }


    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition_target)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        ViewCompat.setTransitionName(transition_target, TRANSITION_IMAGE)
        window.sharedElementEnterTransition?.addListener(onEnd = {
            // NOTE: onEnd gets called before transition truly ends, which causes the transitioned image to flicker
            // NOTE: this is fixed by launching a coroutine on the main thread, which essentially delays the chained
            // NOTE: transition. Im not sure if this is a bug or part of the system (probably system bug)
            launch {
                TransitionManager.beginDelayedTransition(transition_container)
                transition_text.text = "Hello world"
            }
        })

        fab.setOnClickListener {
            TransitionManager.beginDelayedTransition(transition_container)
            transition_text.text = getRandomString(8)
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
