package xyz.velvetmilk.testingtool.net

import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException
import java.net.BindException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SecureServer @Inject constructor(private val sslManager: SSLManager) : CoroutineScope {
    private lateinit var serverSocket: ServerSocket
    private lateinit var acceptedSocket: Socket

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

        if (::serverSocket.isInitialized) {
            serverSocket.close()
        }
        if (::acceptedSocket.isInitialized) {
            acceptedSocket.close()
        }

        Timber.w(String.format("Server caught: %s", exception.localizedMessage))
    }

    // boot up an insecure server using raw sockets (activities want to interact with this
    fun initialise(port: Int) {
        job = Job()

        launch(errorHandler) {
            // ponger
            val sslContext = sslManager.createSSLContext()
            serverSocket = sslContext.serverSocketFactory.createServerSocket(port)
            acceptedSocket = serverSocket.accept()
            parseServerSocketData()
        }
    }

    fun deinitialise() {
        runBlocking(Dispatchers.IO) {
            serverSocket.close()
            acceptedSocket.close()
        }

        job.cancel()
    }

    private fun parseServerSocketData() {
        do {
            val data = acceptedSocket.getInputStream().read()

            Timber.d("Server: " + data.toString())
            if (data == 44) {
                // got a alive message
                Timber.d("Client is alive")
            }
            if (data == 42) {
                // got a ping packet, send pong
                sendPacket(buildPong())
            }
        } while (data != -1)
    }

    private fun buildCheckAlive(): Packet {
        return Packet(88)
    }

    private fun buildPong(): Packet {
        return Packet(24)
    }

    private fun sendPacket(packet: Packet) {
        if (!::acceptedSocket.isInitialized || acceptedSocket.isClosed) {
            return
        }

        launch {
            try {
                acceptedSocket.outputStream.write(packet.value)
            } catch (e: SocketException) {
            } catch (e: IOException) {
            }
        }
    }

    internal fun checkAliveClient() {
        sendPacket(buildCheckAlive())
    }
}
