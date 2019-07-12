package xyz.velvetmilk.testingtool.tools

import timber.log.Timber

class AntiTampering {

    companion object {
        fun runtimeDetection() {
            try {
                throw Exception()
            } catch (e: Exception) {
                for (stackTraceElement in e.stackTrace) {
                    Timber.d(stackTraceElement.className)
                    Timber.d(stackTraceElement.methodName)
                }
            }

        }
    }

}
