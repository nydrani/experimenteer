package xyz.velvetmilk.testingtool.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.selects.SelectClause2
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@kotlinx.coroutines.ExperimentalCoroutinesApi
class CustomActor<E>(coroutineScope: CoroutineScope, block: suspend (E) -> Boolean) : SendChannel<E> {
    // sets up a channel and acts upon messages being sent to it
    // suspends until actor reads the channel
    private val channel = Channel<E>(Channel.RENDEZVOUS)
    private val listeners: MutableList<(Boolean) -> Unit> = mutableListOf()

    init {
        val newContext = coroutineScope.newCoroutineContext(EmptyCoroutineContext)
        coroutineScope.launch(newContext) {
            for (i in channel) {
                Timber.d(String.format("Actor got: %s", i))
                val res = block(i)
                listeners.forEach { it.invoke(res) }
                listeners.clear()
            }
        }
    }


    override val isFull: Boolean
        get() = false

    override val isClosedForSend: Boolean
        get() = channel.isClosedForSend

    override val onSend: SelectClause2<E, SendChannel<E>>
        get() = channel.onSend

    override fun close(cause: Throwable?): Boolean {
        return channel.close(cause)
    }

    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        return channel.invokeOnClose(handler)
    }

    override fun offer(element: E): Boolean {
        return channel.offer(element)
    }

    override suspend fun send(element: E) {
        channel.send(element)
    }

    suspend fun waitForResult(element: E? = null): Boolean {
        element?.let {
            channel.send(it)
        }

        return suspendCoroutine { cont ->
            listeners.add {
                cont.resume(it)
            }
        }
    }
}
