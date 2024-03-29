package xyz.velvetmilk.testingtool

import android.annotation.SuppressLint
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
import xyz.velvetmilk.testingtool.di.ActivityModule
import xyz.velvetmilk.testingtool.di.DaggerActivityComponent
import xyz.velvetmilk.testingtool.net.SslManager
import java.security.SecureRandom
import java.security.Security
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class RNGActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = RNGActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, RNGActivity::class.java)
        }
    }

    @Inject
    lateinit var sslManager: SslManager

    private lateinit var secureRandom: SecureRandom

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    // We actually testing here
    @SuppressLint("SecureRandom")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rng)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // dagger injection
        DaggerActivityComponent.factory()
            .create((application as TestingApp).appComponent, ActivityModule(this))
            .inject(this)

        job = Job()
        disposer = CompositeDisposable()

        sslManager.updateProvider(this)
        secureRandom = SecureRandom()

        printAlgorithms()

        fab.setOnClickListener {
            // reset secure random
            val secureRandom = SecureRandom()
            base_view.text = secureRandom.nextInt().toString()
        }

        fab2.setOnClickListener {
            // reset secure random
            secureRandom.setSeed(byteArrayOf(1, 2, 3, 4))
        }

        fab3.setOnClickListener {
            base_view.text = secureRandom.nextInt().toString()
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


    private fun printAlgorithms() {
        val algorithms = Security.getAlgorithms("SecureRandom")
        val stringBuilder = StringBuilder()

        stringBuilder.appendln(secureRandom.provider.name)
        stringBuilder.appendln(algorithms)

        base_view.text = stringBuilder.toString()
    }
}
