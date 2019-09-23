package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_rng.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import java.security.SecureRandom
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class RNGActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = RNGActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, RNGActivity::class.java)
        }
    }

    private lateinit var secureRandom: SecureRandom

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rng)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        secureRandom = SecureRandom.getInstance("SHA1PRNG", "SC")
        Timber.d(secureRandom.provider.name)

        fab.setOnClickListener {
            base_view.text = secureRandom.nextInt().toString()
        }

        fab2.setOnClickListener {
            // reset secure random
            val secureRandom = SecureRandom.getInstance("SHA1PRNG", "SC")
            base_view.text = secureRandom.nextInt().toString()
        }

        fab3.setOnClickListener {
            // reset secure random
            secureRandom.setSeed(byteArrayOf(1, 2, 3, 4))
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
