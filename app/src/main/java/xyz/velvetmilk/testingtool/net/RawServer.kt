package xyz.velvetmilk.testingtool.net

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import kotlin.coroutines.CoroutineContext

class RawServer : CoroutineScope {
    private lateinit var serverSocket: ServerSocket
    private lateinit var acceptedSocket: Socket

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    // boot up an insecure server using raw sockets (activities want to interact with this
    fun initialise(port: Int) {
        job = Job()

        launch {
            serverSocket = ServerSocket(port)
            acceptedSocket = serverSocket.accept()
            parseServerSocketData(acceptedSocket)
        }
    }

    fun deinitialise() {
        acceptedSocket.close()
        serverSocket.close()

        job.cancel()
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

            Timber.d("Server: " + data.toString())
            if (data == 44) {
                // got a alive message
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

    internal fun checkAliveClient() {
        launch {
            try {
                acceptedSocket.outputStream.write(88)
            } catch (e: SocketException) {
            } catch (e: IOException) {
            }
        }
    }
}
