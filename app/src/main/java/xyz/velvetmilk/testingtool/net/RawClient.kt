package xyz.velvetmilk.testingtool.net

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.SocketException
import kotlin.coroutines.CoroutineContext

class RawClient : CoroutineScope {
    private lateinit var clientSocket: Socket
    private lateinit var timer: Instant

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    // boot up an insecure server using raw sockets (activities want to interact with this
    fun initialise(port: Int) {
        job = Job()

        // load client socket in background thread
        launch {
            // pinger
            clientSocket = Socket(InetAddress.getLocalHost(), port)
            parseClientSocketData(clientSocket)
        }
    }

    fun deinitialise() {
        clientSocket.close()

        job.cancel()
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

            Timber.d("Client: " + data.toString())
            if (data == 24) {
                // got a pong message
                Timber.d(String.format("Ping time: %d", ChronoUnit.MILLIS.between(timer, Instant.now())))
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

    internal fun pingServer() {
        launch {
            timer = Instant.now()
            try {
                clientSocket.outputStream.write(42)
            } catch (e: SocketException) {
            } catch (e: IOException) {
            }
        }
    }
}
