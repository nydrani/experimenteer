package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_secure_socket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber
import java.io.IOException
import java.net.*
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.*
import kotlin.coroutines.CoroutineContext


class SecureSocketActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SecureSocketActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, SecureSocketActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var serverSocket: SSLServerSocket
    private lateinit var acceptedSocket: SSLSocket
    private lateinit var connectedSocket: SSLSocket
    private lateinit var timer: Instant


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secure_socket)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        checkSecurity()

        // update the provider so we get the latest security stuff
        launch {
            updateProvider()
            val sslContext = createSSLContext()
            initSockets(sslContext)
        }

        fab.setOnClickListener {
            launch(Dispatchers.IO) {
                pingServer()
            }
        }

        fab2.setOnClickListener {
            launch(Dispatchers.IO) {
                checkAliveClient()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // close sockets
        if (::connectedSocket.isInitialized) {
            launch(Dispatchers.IO) {
                connectedSocket.close()
            }
        }
        if (::acceptedSocket.isInitialized) {
            launch(Dispatchers.IO) {
                acceptedSocket.close()
            }
        }
        if (::serverSocket.isInitialized) {
            launch(Dispatchers.IO) {
                serverSocket.close()
            }
        }

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


    private fun pingServer() {
        timer = Instant.now()
        connectedSocket.outputStream.write(42)
    }

    private fun checkAliveClient() {
        acceptedSocket.outputStream.write(88)
    }

    private fun parseServerSocketData(socket: Socket) {
        do {
            val data: Int
            try {
                data = socket.getInputStream().read()
            } catch (e: SocketException) {
                break
            } catch (e: IOException) {
                break
            }

            Timber.d(data.toString())
            if (data == 44) {
                // got a alive message
                launch(Dispatchers.Main) {
                    socket_view.text = "Client is alive"
                }
            }
            if (data == 42) {
                // got a ping packet, send pong
                socket.getOutputStream().write(24)
            }
        } while (data != -1)
    }

    private fun parseClientSocketData(socket: Socket) {
        do {
            val data: Int
            try {
                data = socket.getInputStream().read()
            } catch (e: SocketException) {
                break
            } catch (e: IOException) {
                break
            }

            Timber.d(data.toString())
            if (data == 24) {
                // got a pong message
                launch(Dispatchers.Main) {
                    socket_view.text = Duration.between(timer, Instant.now()).toMillis().toString()
                }
            }
            if (data == 88) {
                // got a alive packet, send yes i am alive packet
                socket.getOutputStream().write(44)
            }
        } while (data != -1)
    }

    private fun updateProvider() {
        try {
            ProviderInstaller.installIfNeeded(this@SecureSocketActivity)
            socket_view.text = "Provider up to date"
        } catch (e: GooglePlayServicesRepairableException) {
            GoogleApiAvailability.getInstance().showErrorNotification(this@SecureSocketActivity, e.connectionStatusCode)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Timber.d(e.localizedMessage)
            socket_view.text = e.localizedMessage
        }
    }

    private fun createSSLContext(): SSLContext {
        // trust everyone
        val tm = arrayOf(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                Timber.d("checkClientTrusted")
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                Timber.d("checkServerTrusted")
            }
        })

        // TODO: load keystore with selfsigned cert and private pem
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

        // create keymanager for keystore
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, null)

        // load sslcontext with keymanager and all trusting trustmanager
        val sslContext = SSLContext.getInstance("TLSv1.3")
        sslContext.init(keyManagerFactory.keyManagers, tm, null)

        return sslContext
    }

    private fun initSockets(sslContext: SSLContext) {
        // load server socket in background thread
        launch(Dispatchers.IO) {
            // ponger
            try {
                serverSocket = sslContext.serverSocketFactory.createServerSocket(55555) as SSLServerSocket
                //serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(55555) as SSLServerSocket
                acceptedSocket = serverSocket.accept() as SSLSocket
                //Timber.d("ServerSocket")
                //printSocketDetails(acceptedSocket)
            } catch (e: IOException) {
                // cant open port on this
                socket_view.text = "Cannot open server socket"
            }

            parseServerSocketData(acceptedSocket)
        }

        // load client socket in background thread
        launch(Dispatchers.IO) {
            // pinger
            try {
                connectedSocket = sslContext.socketFactory.createSocket(InetAddress.getLocalHost(), 55555) as SSLSocket
                //connectedSocket = sslContext.socketFactory.createSocket("192.168.105.14", 55555) as SSLSocket
                //connectedSocket = SSLSocketFactory.getDefault().createSocket(InetAddress.getLocalHost(), 55555) as SSLSocket
                //connectedSocket = SSLSocketFactory.getDefault().createSocket("192.168.105.14", 55555) as SSLSocket
                //Timber.d("ClientSocket")
                //printSocketDetails(connectedSocket)
                connectedSocket.startHandshake()
                connectedSocket.addHandshakeCompletedListener {
                    Timber.d("handleshake complete")
                    Timber.d(it.session.protocol)
                }
            } catch (e: IOException) {
                // cant open port on this
                socket_view.text = "Cannot connect to socket"
            }
            parseClientSocketData(connectedSocket)
        }
    }

    private fun checkSecurity() {
        // prints out default security properties
        Timber.d(java.security.Security.getProperty("ssl.ServerSocketFactory.provider"))
        Timber.d(java.security.Security.getProperty("ssl.SocketFactory.provider"))
        Timber.d(java.security.Security.getProperty("ssl.KeyManagerFactory.algorithm"))
        Timber.d(java.security.Security.getProperty("keystore.type"))
    }

    private fun printSocketDetails(socket: SSLSocket) {
        for (prot in socket.supportedProtocols) {
            Timber.d(prot)
        }
        Timber.d("Enabled")
        for (prot in socket.enabledProtocols) {
            Timber.d(prot)
        }
        for (ciph in socket.supportedCipherSuites) {
            Timber.d(ciph)
        }
        Timber.d("Enabled")
        for (ciph in socket.enabledCipherSuites) {
            Timber.d(ciph)
        }
    }
}
