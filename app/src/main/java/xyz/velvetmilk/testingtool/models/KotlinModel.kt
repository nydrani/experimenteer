package xyz.velvetmilk.testingtool.models

import timber.log.Timber

class KotlinModel {

    // NOTE: order of calls is: 3 -> 7 -> 1
    // NOTE: constructor calls is last in the chain
    constructor() {
        Timber.d("1")
    }

    // NOTE: order of calls is: 3 -> 7 -> 2 -> string
    constructor(string: String) {
        Timber.d("2")
        Timber.d(string)
    }

    init {
        Timber.d("3")
    }

    // NOTE: this get initialised once and then cached on future construction
    // NOTE: order of calls is: 4 -> 6 -> 3 -> 7 -> 1 (on caching, 4 and 6 doesn't get called)
    companion object {
        init {
            Timber.d("4")
        }

        fun companionCall() {
            Timber.d("5")
        }

        init {
            Timber.d("6")
        }

    }

    init {
        Timber.d("7")
    }
}
