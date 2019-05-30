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
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber
import java.io.IOException
import java.net.*
import kotlin.coroutines.CoroutineContext


class SocketActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SocketActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, SocketActivity::class.java)
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
        setContentView(R.layout.activity_socket)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()


        // load server socket in background thread
        launch(Dispatchers.IO) {
            // ponger
            serverSocket = ServerSocket(55555)
            acceptedSocket = serverSocket.accept()
            parseServerSocketData(acceptedSocket)
        }

        // load client socket in background thread
        launch(Dispatchers.IO) {
            // pinger
            connectedSocket = Socket(InetAddress.getLocalHost(), 55555)
            parseClientSocketData(connectedSocket)
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

        connectedSocket.close()
        acceptedSocket.close()
        serverSocket.close()

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
}
