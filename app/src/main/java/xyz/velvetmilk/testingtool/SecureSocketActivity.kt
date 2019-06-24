package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_secure_socket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import xyz.velvetmilk.testingtool.di.ActivityModule
import xyz.velvetmilk.testingtool.di.DaggerActivityComponent
import xyz.velvetmilk.testingtool.net.SSLManager
import xyz.velvetmilk.testingtool.net.SecureClient
import xyz.velvetmilk.testingtool.net.SecureServer
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SecureSocketActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SecureSocketActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, SecureSocketActivity::class.java)
        }
    }

    @Inject
    lateinit var sslManager: SSLManager
    @Inject
    lateinit var secureServer: SecureServer
    @Inject
    lateinit var secureClient: SecureClient

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secure_socket)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // dagger injection
        DaggerActivityComponent.factory()
            .create((application as TestingApp).appComponent, ActivityModule(this))
            .inject(this)

        job = Job()
        disposer = CompositeDisposable()

        // TODO: Issues with coroutines + blocking resources
        // https://github.com/Kotlin/kotlinx.coroutines/issues/1191
        // https://github.com/Kotlin/kotlinx.coroutines/issues/1044
        // update the provider so we get the latest security stuff and init sockets after
        launch {
            sslManager.updateProvider(this@SecureSocketActivity)
            secureServer.initialise(55555)
            secureClient.initialise(55555)
        }

        fab.setOnClickListener {
            launch(Dispatchers.IO) {
                secureClient.pingServer()
            }
        }

        fab2.setOnClickListener {
            launch(Dispatchers.IO) {
                secureServer.checkAliveClient()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        secureClient.deinitialise()
        secureServer.deinitialise()

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
