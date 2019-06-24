package xyz.velvetmilk.testingtool.services

import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

// NOTE: This runner runs in the background limited by its scope and scope of the ui
// NOTE: it looks like the scope of the UI thread exists until the application context dies
class BackgroundCoroutineRunner : CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    // Bootup functions (must be called)
    fun init() {
        job = Job()

        launch {
            dance()
        }
    }

    fun deinit() {
        job.cancel()
    }

    // Dance for a minute
    private suspend fun dance() {
        for (i in 0 until 10) {
            Timber.d("dance %d", i)
            delay(6000)
        }
    }
}
