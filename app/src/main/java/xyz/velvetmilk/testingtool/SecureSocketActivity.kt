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
import kotlinx.coroutines.*
import org.spongycastle.util.io.pem.PemReader
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.StringReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext


class SecureSocketActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SecureSocketActivity::class.simpleName

        private const val ALIAS_CERT = "ALIAS_CERT"
        private const val ALIAS_KEY = "ALIAS_KEY"

        fun buildIntent(context: Context): Intent {
            return Intent(context, SecureSocketActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var serverSocket: ServerSocket
    private lateinit var acceptedSocket: Socket
    private lateinit var connectedSocket: Socket
    private lateinit var timer: Instant


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secure_socket)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        checkSecurity()

        val sslContext = createSSLContext()
        // update the provider so we get the latest security stuff
        launch {
            updateProvider()
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

        // NOTE: Need to run this as blocking on IO thread otherwise it cries
        runBlocking(Dispatchers.IO) {
            connectedSocket.close()
            acceptedSocket.close()
            serverSocket.close()
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


    private fun pingServer() {
        timer = Instant.now()
        try {
            connectedSocket.outputStream.write(42)
        } catch (e: SocketException) {
        } catch (e: IOException) {
        }
    }

    private fun checkAliveClient() {
        try {
            acceptedSocket.outputStream.write(88)
        } catch (e: SocketException) {
        } catch (e: IOException) {
        }
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
                try {
                    socket.getOutputStream().write(24)
                } catch (e: SocketException) {
                } catch (e: IOException) {
                }
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
                try {
                    socket.getOutputStream().write(44)
                } catch (e: SocketException) {
                } catch (e: IOException) {
                }
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

        val certificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIICpDCCAYwCCQCEHPpkmi0HNTANBgkqhkiG9w0BAQsFADAUMRIwEAYDVQQDDAls\n" +
                "b2NhbGhvc3QwHhcNMTkwNTMwMDAzMDQ5WhcNMTkwNjI5MDAzMDQ5WjAUMRIwEAYD\n" +
                "VQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC3\n" +
                "AYYnqmU6+dQm6VkqKDy46iNv0Fc+aj6FDFQGI/dmDaI6Lr43nVcVGhl/hYZY9MRB\n" +
                "PaNOTnngY2BQR6zhk1DMFmiddPPO78B75zQUlj20b9P6LrLIkdxlGLq5kbPEOeB7\n" +
                "Z8ZraWtmgiGqkUaZ172lsSomxt7Y/btCClvuV4BkxdJZh6cKWDBcNeeI5T/b17cG\n" +
                "1XSj+ZMhzWl5lZgwviEgHzmldGgB/gtFxpS5Dxq/aR145PWlgXvFfDmryPXm1zAy\n" +
                "X3BNulDW9JV8UxCRm2Y0shxtL6haN5Vl+ZAUNwiHjJpYh7rEJaIXKLBaljplbW3Y\n" +
                "ULac1r9eCOuijzMaEJElAgMBAAEwDQYJKoZIhvcNAQELBQADggEBALVf+oZDqM2W\n" +
                "dTjdvGEkprKmFf2gDl0u/OkhROtBWNhAkiHtx0zITHzwY4fh/WEiJI+H5Q4szxAZ\n" +
                "U9Sw2/xcumTKPOyN4CVLaU0XpPHnxjuEValgQZ/2wNKB2NzjYx87dCl3qCN7YRzH\n" +
                "oZyn4vjKY/0mIUS2JfFSUb6hxEpmqM3O4v63rwQjQeHdFqshr3wGE0VmDdHEr7oQ\n" +
                "J77KpW445JB5AmjNwrLZGnlc4ICDpmzqEzdNMZEH0MJ1ZU2hQPjKyhRCP/wq0wuf\n" +
                "/4kHBPiCvoZXN3Y2wuuMiGccRqO7TAMUZuNCAN9nct/wxNOueAvxndGksB06p27K\n" +
                "kW3D+VbfobI=\n" +
                "-----END CERTIFICATE-----"

        val privKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC3AYYnqmU6+dQm\n" +
                "6VkqKDy46iNv0Fc+aj6FDFQGI/dmDaI6Lr43nVcVGhl/hYZY9MRBPaNOTnngY2BQ\n" +
                "R6zhk1DMFmiddPPO78B75zQUlj20b9P6LrLIkdxlGLq5kbPEOeB7Z8ZraWtmgiGq\n" +
                "kUaZ172lsSomxt7Y/btCClvuV4BkxdJZh6cKWDBcNeeI5T/b17cG1XSj+ZMhzWl5\n" +
                "lZgwviEgHzmldGgB/gtFxpS5Dxq/aR145PWlgXvFfDmryPXm1zAyX3BNulDW9JV8\n" +
                "UxCRm2Y0shxtL6haN5Vl+ZAUNwiHjJpYh7rEJaIXKLBaljplbW3YULac1r9eCOui\n" +
                "jzMaEJElAgMBAAECggEAch4GLQfpVixhqd4LFum+a8S9UkVFkgsob0jvwGB/LmlE\n" +
                "ByoMvZtkqSj8S3PVAfWnx9MK2ZLAFzeA4K7BVGzLjmhO1hkOy7Tff6P96vSBB9pQ\n" +
                "AgtKux2RpI0WKw97XNqGbA2bQnGbYnG1UqqCrv5EKWVfloyefAtE0PqqtuZjjvJq\n" +
                "PSU9dRBdHWFM90CGalNhN3c9aWbtfq6TdnjJYExBfGDLZReL+BNrN+rfAnBatAV2\n" +
                "B/idlODviz3sO5nMwBGc1di9kdldLKa2zSLyQScjfMz0M2r76rvLfelKgTipOPWZ\n" +
                "Vt0bXL41lnYmosuKPpjGYOXX+gCIlWl19hqMlvTgRQKBgQDlpJfKrBHljbN/isAi\n" +
                "5gjJnqIsuTEu6tdWGxxzdCTGN5FFhWKiqO7ac8Kpl9GeTIRsR4piokRbeJoVIDuC\n" +
                "imoumXoy0BrUFqgqFmcZSt31hw1UrxbJs1L7gw1BypwQ2nPdcQ3TndUPEitsIgT6\n" +
                "F1L2iPf1FzmTTJGXBDopHTAJIwKBgQDMAqKoIQYtQPEOJ3EhrZbgwrJZ+QzxBjs8\n" +
                "8Cifxy5+vffzszQvp3QjBkIkNrlkPdJhqm/rVckUK/MNnWL2537cIMgaSdF7X3ke\n" +
                "QVJLut62MVigRo/k5i0Lok8YpM4csZ7aYXgY8Et16wAtwf9ALc+qRX3fS/emHuV3\n" +
                "PMBNoC61FwKBgF0hPPXShoeDyfHFgVol1BaAIODmUc3nK2EeKZGg3nEMg/uftnqk\n" +
                "7AjnvWAt07LQ8TtAYBzUFcjKxiIfKkMsgxdW4rnMr7SnY5d98l3NgOpb1MotH564\n" +
                "/MRHR48Q7e3fyfFaMfNKJOexxK5btYz+/IRC09wviQf1m22VJ0kZLyvlAoGBAJIH\n" +
                "hOSyFtjHtoS6KNnBhtFFrCKJCgFww3BgO8P68EYatuSDLuS5iYEUlr2TSmr9cZ0l\n" +
                "Qc2Db1Z4CxeAw/kWRZFOCc3/bupPscO9YCoDDi397oFzYMapGC9OtC1gsoSJg7qv\n" +
                "sCaxwmIan9shFROcdxxbd7khiJgpX3lVTBWEhkprAoGAF5fyoAZLx0fq+q8YI+x/\n" +
                "Fh8N7sF9K/KNTfCKVr6E0N6T+/HdMisLHVg3YC0J+b/p5yV/MJC7uCE8dsk5qAwM\n" +
                "zFpw/Jpi9Od0iHDv+JzSS4S7evTyxuc8jPqU2IFTCs3D9uszk5i3I4mUzQTvBO+t\n" +
                "nn47QjrY8aSbiHSYPQo5jfI=\n" +
                "-----END PRIVATE KEY-----"

        var privKeyDER: ByteArray = byteArrayOf()
        PemReader(StringReader(privKey)).use {
            privKeyDER = it.readPemObject().content
        }

        // generate certificate obj
        val cf = CertificateFactory.getInstance("X.509")
        val cert = cf.generateCertificate(ByteArrayInputStream(certificate.toByteArray(Charsets.UTF_8)))

        // generate private key obj
        val kf = KeyFactory.getInstance("RSA")
        val key = kf.generatePrivate(PKCS8EncodedKeySpec(privKeyDER))

        // load keystore with selfsigned cert and private pem
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null)
        keyStore.setCertificateEntry(ALIAS_CERT, cert)
        keyStore.setKeyEntry(ALIAS_KEY, key, null, arrayOf(cert))

        // create keymanager for keystore
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, null)

        // load sslcontext with keymanager and all trusting trustmanager
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(keyManagerFactory.keyManagers, tm, null)

        return sslContext
    }

    // TODO: Issues with coroutines + blocking resources
    // https://github.com/Kotlin/kotlinx.coroutines/issues/1191
    // https://github.com/Kotlin/kotlinx.coroutines/issues/1044
    private fun initSockets(sslContext: SSLContext) {
        // load server socket in background thread
        launch(Dispatchers.IO) {
            // ponger
            try {
                sslContext.serverSocketFactory.createServerSocket(55555).use { serverSocket ->
                    this@SecureSocketActivity.serverSocket = serverSocket
                    serverSocket.accept().use { acceptedSocket ->
                        this@SecureSocketActivity.acceptedSocket = acceptedSocket
                        parseServerSocketData(acceptedSocket)
                    }
                }
            } catch (e: IOException) {
                // cant open port on this
                e.printStackTrace()
                socket_view.text = "Cannot open server socket"
            }
        }

        // load client socket in background thread
        launch(Dispatchers.IO) {
            // pinger
            try {
                sslContext.socketFactory.createSocket(InetAddress.getLocalHost(), 55555).use {
                    this@SecureSocketActivity.connectedSocket = it
                    parseClientSocketData(it)
                }
            } catch (e: IOException) {
                // cant open port on this
                e.printStackTrace()
                socket_view.text = "Cannot connect to socket"
            }
        }
    }

    private fun checkSecurity() {
        // prints out default security properties
        Timber.d(java.security.Security.getProperty("ssl.ServerSocketFactory.provider"))
        Timber.d(java.security.Security.getProperty("ssl.SocketFactory.provider"))
        Timber.d(java.security.Security.getProperty("ssl.KeyManagerFactory.algorithm"))
        Timber.d(java.security.Security.getProperty("keystore.type"))
    }
}
