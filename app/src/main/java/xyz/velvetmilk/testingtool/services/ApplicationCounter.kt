package xyz.velvetmilk.testingtool.services

import timber.log.Timber

class ApplicationCounter {

    companion object {
        private val TAG = ApplicationCounter::class.simpleName
    }

    var counter = 0
        private set

    init {
        Timber.d("INIT APPLICATIONCOUNTER")
    }

    fun incrementCounter() {
        counter++
    }

    fun decrementCounter() {
        counter--
    }
}