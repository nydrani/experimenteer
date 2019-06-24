package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_dagger.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.velvetmilk.testingtool.di.ActivityModule
import xyz.velvetmilk.testingtool.di.DaggerActivityComponent
import xyz.velvetmilk.testingtool.services.ActivityCounter
import xyz.velvetmilk.testingtool.services.ApplicationCounter
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class DaggerActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = DaggerActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, DaggerActivity::class.java)
        }
    }

    @Inject lateinit var appCounter: ApplicationCounter
    @Inject lateinit var activityCounter: ActivityCounter

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dagger)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // dagger injection
        DaggerActivityComponent.factory()
            .create((application as TestingApp).appComponent, ActivityModule(this))
            .inject(this)

        job = Job()
        disposer = CompositeDisposable()

        fab.setOnClickListener {
            appCounter.incrementCounter()
            activityCounter.incrementCounter()

            base_view.text = String.format("AppCounter: %d | ActivityCounter: %d\n", appCounter.counter, activityCounter.counter)
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
