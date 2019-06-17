package xyz.velvetmilk.testingtool

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_animation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


class AnimationActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = AnimationActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, AnimationActivity::class.java)
        }
    }

    private var opaque = true
    private var content = true
    private var animationFinished = true

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
            duration = 1600
            addUpdateListener {
                ring_inner_view.rotation = it.animatedValue as Float
            }
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
            repeatCount = Animation.INFINITE
            start()
        }

        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 4000
            addUpdateListener {
                ring_outer_view.rotation = it.animatedValue as Float
            }
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
            repeatCount = Animation.INFINITE
            reverse()
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
            if (opaque) {
                processing_container.animate().alpha(0.0f).duration = 1000
                opaque = false
            } else {
                processing_container.animate().alpha(1.0f).duration = 1000
                opaque = true
            }

            val cx = image2_view.width / 2
            val cy = image2_view.height / 2
            val initialRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()

            if (opaque) {
                val anim = ViewAnimationUtils.createCircularReveal(image2_view, cx, cy, initialRadius, 0f)
                anim.addListener(onEnd = {
                    image2_view.visibility = View.INVISIBLE
                })
                anim.start()
            } else {
                val anim = ViewAnimationUtils.createCircularReveal(image2_view, cx, cy, 0f, initialRadius)
                anim.addListener(onStart = {
                    image2_view.visibility = View.VISIBLE
                })
                anim.start()
            }
        }

        fab2.setOnClickListener {
            // early exit if animation is still running
            if (!animationFinished) {
                return@setOnClickListener
            }

            val cx = fab2.x.toInt() + fab2.width / 2
            val cy = fab2.y.toInt() - fab2.height / 2

            val mx = Math.max(cx.toDouble(), (content_container.width - cx).toDouble())
            val my = Math.max(cy.toDouble(), (content_container.height - cy).toDouble())
            val initialRadius = Math.hypot(mx, my).toFloat()

            if (content) {
                val anim = ViewAnimationUtils.createCircularReveal(animation_container, cx, cy, initialRadius, 0f)
                anim.addListener(onEnd = {
                    animation_container.visibility = View.INVISIBLE
                    animationFinished = true
                    content = false
                })
                anim.start()
            } else {
                val anim = ViewAnimationUtils.createCircularReveal(animation_container, cx, cy, 0f, initialRadius)
                anim.addListener(onStart = {
                    animation_container.visibility = View.VISIBLE
                }, onEnd = {
                    animationFinished = true
                    content = true
                })
                anim.start()
            }

            animationFinished = false
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
