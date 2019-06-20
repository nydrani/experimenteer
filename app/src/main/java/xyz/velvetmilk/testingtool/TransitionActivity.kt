package xyz.velvetmilk.testingtool

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_transition.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.tools.getRandomString
import kotlin.coroutines.CoroutineContext


class TransitionActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = TransitionActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, TransitionActivity::class.java)
        }
    }


    private var count = 0
    private var transitioned = false
    private var autoTransitioned = false

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

        transition_source.setOnClickListener {
            val transition = ActivityOptions.makeSceneTransitionAnimation(this,
                transition_source,
                TransitionTargetActivity.TRANSITION_IMAGE)

            startActivity(TransitionTargetActivity.buildIntent(this), transition.toBundle())
        }

        fab.setOnClickListener {
            TransitionManager.beginDelayedTransition(transition_container)
            transitioned = !transitioned
            if (transitioned) {
                transition_text.visibility = View.GONE
                transition_content.gravity = Gravity.NO_GRAVITY
            } else {
                transition_text.visibility = View.VISIBLE
                transition_content.gravity = Gravity.CENTER
            }
        }

        fab2.setOnClickListener {
            val customTransition = AutoTransition()
            customTransition.addListener(object : Transition.TransitionListener {
                override fun onTransitionCancel(transition: Transition) {
                }

                override fun onTransitionEnd(transition: Transition) {
                    // quit after enough times
                    if (count >= 10) {
                        fab2.visibility = View.VISIBLE
                        return
                    }

                    TransitionManager.beginDelayedTransition(transition_container, customTransition)
                    autoTransitioned = !autoTransitioned
                    if (autoTransitioned) {
                        transition_text2.text = getRandomString(10)
                    } else {
                        transition_text2.text = getRandomString(1)
                    }

                    count++
                }

                override fun onTransitionPause(transition: Transition) {
                }

                override fun onTransitionResume(transition: Transition) {
                }

                override fun onTransitionStart(transition: Transition) {
                }
            })

            TransitionManager.beginDelayedTransition(transition_container, customTransition)
            autoTransitioned = !autoTransitioned
            if (autoTransitioned) {
                transition_text2.text = getRandomString(10)
            } else {
                transition_text2.text = getRandomString(1)
            }

            // reset auto transition counter
            count = 0
            fab2.visibility = View.GONE
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
