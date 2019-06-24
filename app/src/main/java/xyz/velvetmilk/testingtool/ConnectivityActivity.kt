package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_connectivity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.CoroutineContext

class ConnectivityActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = ConnectivityActivity::class.simpleName
        private const val TEST_HOST_NAME = "airpayapp.com.au"
        private const val TEST_PORT = 443

        fun buildIntent(context: Context): Intent {
            return Intent(context, ConnectivityActivity::class.java)
        }
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val disposer = CompositeDisposable()
    private val builder = StringBuilder()
    private lateinit var connectivityManager: ConnectivityManager

    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            Timber.d("onAvailable | %s", network.toString())
            builder.appendln(String.format("onAvailable | %s", network.toString()))

            launch(Dispatchers.Main) {
                network_view.text = builder.toString()
            }

            // check network connectivity at this point
            Timber.d("%b", connectivityManager.activeNetworkInfo?.isConnected)
            Timber.d("%b", isInternetConnected())
            Timber.d("%b", isServerAvailable(TEST_HOST_NAME, TEST_PORT))
        }

        override fun onCapabilitiesChanged(network: Network?, networkCapabilities: NetworkCapabilities?) {
            Timber.d("onCapabilitiesChanged | %s %s", network.toString(), networkCapabilities.toString())
            builder.appendln(String.format("onCapabilitiesChanged | %s | %s", network.toString(), networkCapabilities.toString()))

            launch(Dispatchers.Main) {
                network_view.text = builder.toString()
            }
        }

        override fun onLinkPropertiesChanged(network: Network?, linkProperties: LinkProperties?) {
            Timber.d("onLinkPropertiesChanged | %s %s", network.toString(), linkProperties.toString())
            builder.appendln(String.format("onLinkPropertiesChanged | %s | %s", network.toString(), linkProperties.toString()))

            launch(Dispatchers.Main) {
                network_view.text = builder.toString()
            }
        }

        override fun onLosing(network: Network?, maxMsToLive: Int) {
            Timber.d("onLosing | %s | %d", network.toString(), maxMsToLive)
            builder.appendln(String.format("onLosing | %s | %d", network.toString(), maxMsToLive))

            launch(Dispatchers.Main) {
                network_view.text = builder.toString()
            }
        }

        override fun onLost(network: Network?) {
            Timber.d("onLost | %s", network.toString())
            builder.appendln(String.format("onLost | %s", network.toString()))

            launch(Dispatchers.Main) {
                network_view.text = builder.toString()
            }
        }

        override fun onUnavailable() {
            Timber.d("onUnavailable")
            builder.appendln("onUnavailable")

            launch(Dispatchers.Main) {
                network_view.text = builder.toString()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connectivity)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fab.setOnClickListener {
            val formatted = String.format("ConnectivityManager.activeNetworkInfo.isConnected | %b",
                connectivityManager.activeNetworkInfo?.isConnected ?: false)
            Snackbar.make(it, formatted, Snackbar.LENGTH_LONG).show()
        }

        fab2.setOnClickListener {
            launch(Dispatchers.IO) {
                val isConnected = isInternetConnected()
                Snackbar.make(
                    it,
                    String.format("isInternetConnected | %b", isConnected),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        fab3.setOnClickListener {
            launch(Dispatchers.IO) {
                val isConnected = isServerAvailable(TEST_HOST_NAME, TEST_PORT)
                Snackbar.make(
                    it,
                    String.format("isServerAvailable | %s | %d | %b", TEST_HOST_NAME, TEST_PORT, isConnected),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        fab4.setOnClickListener {
            Snackbar.make(it, "Attaching listener to network info", Snackbar.LENGTH_SHORT).show()

            connectivityManager.registerDefaultNetworkCallback(connectivityCallback)
        }

        fab5.setOnClickListener {
            Snackbar.make(it, "Detaching listener from network info", Snackbar.LENGTH_SHORT).show()
            builder.clear()

            try {
                connectivityManager.unregisterNetworkCallback(connectivityCallback)
            } catch (e: IllegalArgumentException) {
                // did this at a bad time
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            connectivityManager.unregisterNetworkCallback(connectivityCallback)
        } catch (e: IllegalArgumentException) {
            // did this at a bad time
        }

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


    private fun isInternetConnected(): Boolean {
        // google dns, cloudflare dns, opendns
        val hostNames = listOf("8.8.8.8", "1.1.1.1", "208.69.38.205")
        val totalTimeoutMs = 1000

        for (host in hostNames) {
            try {
                // Connect to host to check for connection
                val timeoutMs = totalTimeoutMs / hostNames.size
                val socket = Socket()
                val socketAddress = InetSocketAddress(host, 53)

                socket.connect(socketAddress, timeoutMs)
                socket.close()

                return true
            } catch (e: IOException) {
                // unable to establish connection
                // keep trying
            }
        }

        // nothing connected
        return false
    }

    private fun isServerAvailable(host: String, port: Int): Boolean {
        try {
            // Connect to host to check for connection
            val timeoutMs = 1000
            val socket = Socket()
            val socketAddress = InetSocketAddress(host, port)

            socket.connect(socketAddress, timeoutMs)
            socket.close()

            return true
        } catch (e: IOException) {
            // unable to establish connection
        } catch (e: IllegalArgumentException) {
            // issue with host resolution
        }

        // nothing connected
        return false
    }
}
