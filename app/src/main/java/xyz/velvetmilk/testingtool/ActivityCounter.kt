package xyz.velvetmilk.testingtool

import timber.log.Timber

class ActivityCounter {

    companion object {
        private val TAG = ActivityCounter::class.simpleName
    }

    var counter = 0
        private set

    init {
        Timber.d("INIT ACTIVITYCOUNTER")
    }

    fun incrementCounter() {
        counter++
    }

    fun decrementCounter() {
        counter--
    }
}