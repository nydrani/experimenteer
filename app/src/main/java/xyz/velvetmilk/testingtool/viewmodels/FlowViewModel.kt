package xyz.velvetmilk.testingtool.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class FlowViewModel : ViewModel() {
    // data source
    private val flowObj = flow {
        var count = 0
        while (true) {
            delay(1000)
            emit(count)
            count++
        }
    }

    // read from data source
    @ExperimentalCoroutinesApi
    val countData : LiveData<Int> = flowObj
        .map {
            if (Random.nextBoolean()) {
                delay(900)
            }
            it
        }
        .flowOn(Dispatchers.Default)
        .asLiveData()
}
