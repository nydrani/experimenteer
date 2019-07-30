package xyz.velvetmilk.testingtool.net

import kotlinx.coroutines.*
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.io.IOException
import java.net.BindException
import java.net.InetAddress
import java.net.Socket
import java.net.SocketException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SecureClient @Inject constructor(private val sslManager: SslManager) : CoroutineScope {
    private lateinit var clientSocket: Socket
    private lateinit var timer: Instant

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            is SocketException -> {
            }
            is BindException -> {
            }
            is IOException -> {
            }
            else -> {}
        }

        if (::clientSocket.isInitialized) {
            clientSocket.close()
        }

        Timber.w(String.format("Client caught: %s", exception.localizedMessage))
    }

    // boot up an insecure server using raw sockets (activities want to interact with this
    fun initialise(port: Int) {
        job = Job()

        // load client socket in background thread
        launch(errorHandler) {
            // pinger
            val sslContext = sslManager.createSSLContext()
            clientSocket = sslContext.socketFactory.createSocket(InetAddress.getLocalHost(), port)
            parseClientSocketData()
        }
    }

    fun deinitialise() {
        runBlocking(Dispatchers.IO) {
            clientSocket.close()
        }

        job.cancel()
    }

    private fun parseClientSocketData() {
        do {
            val data = clientSocket.getInputStream().read()

            Timber.d("Client: " + data.toString())
            if (data == 24) {
                // got a pong message
                Timber.d(String.format("Ping time: %d", ChronoUnit.MILLIS.between(timer, Instant.now())))
            }
            if (data == 88) {
                // got a alive packet, send yes i am alive packet
                sendPacket(buildConfirmAlive())
            }
        } while (data != -1)
    }

    private fun buildPing(): Packet {
        return Packet(42)
    }

    private fun buildConfirmAlive(): Packet {
        return Packet(44)
    }

    private fun sendPacket(packet: Packet) {
        if (!::clientSocket.isInitialized || clientSocket.isClosed) {
            return
        }

        launch {
            try {
                clientSocket.outputStream.write(packet.value)
            } catch (e: SocketException) {
            } catch (e: IOException) {
            }
        }
    }

    internal fun pingServer() {
        timer = Instant.now()
        sendPacket(buildPing())
    }
}
