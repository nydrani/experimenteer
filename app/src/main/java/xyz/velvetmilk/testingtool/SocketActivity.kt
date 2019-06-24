package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_socket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import xyz.velvetmilk.testingtool.di.ActivityModule
import xyz.velvetmilk.testingtool.di.DaggerActivityComponent
import xyz.velvetmilk.testingtool.net.RawClient
import xyz.velvetmilk.testingtool.net.RawServer
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class SocketActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SocketActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, SocketActivity::class.java)
        }
    }

    @Inject
    lateinit var rawServer: RawServer
    @Inject
    lateinit var rawClient: RawClient

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // dagger injection
        DaggerActivityComponent.factory()
            .create((application as TestingApp).appComponent, ActivityModule(this))
            .inject(this)

        job = Job()
        disposer = CompositeDisposable()

        // setup server and client
        rawServer.initialise(44444)
        rawClient.initialise(44444)

        fab.setOnClickListener {
            rawClient.pingServer()
        }

        fab2.setOnClickListener {
            rawServer.checkAliveClient()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        rawClient.deinitialise()
        rawServer.deinitialise()

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
