package xyz.velvetmilk.testingtool

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_animation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random


class AnimationActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = AnimationActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, AnimationActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1000
            addUpdateListener {
                base_view.rotation = it.animatedValue as Float
            }
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
            repeatCount = Animation.INFINITE
            start()
        }

        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1000
            addUpdateListener {
                image_view.rotation = it.animatedValue as Float
            }
            repeatMode = ValueAnimator.REVERSE
            repeatCount = Animation.INFINITE
            start()
        }

        fab.setOnClickListener {
            base_view.text = Random.nextInt().toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
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
}
