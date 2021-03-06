package com.victorrendina.mvi.sample.counter

import android.util.Log
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.di.InjectableViewModelFactory
import com.victorrendina.mvi.sample.framework.BaseViewModelArgs
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
data class CounterArgs(val initialCount: Int) : MviArgs

data class CounterViewState(val count: Int = 0) : MviState {
    // This constructor will automatically be called to create the initial view state if arguments of type CounterArgs
    // have been passed to the view.
    @Suppress("unused")
    constructor(arguments: CounterArgs) : this(count = arguments.initialCount)
}

@MviViewModel
class CounterViewModel(
    initialState: CounterViewState,
    arguments: CounterArgs?
) : BaseViewModelArgs<CounterViewState, CounterArgs>(initialState, arguments) {

    init {
        logStateChanges()
    }

    fun increaseCount() {
        setState {
            Log.d(tag, "Reducer running on ${Thread.currentThread().name}")
            copy(count = count + 1)
        }

        withState {
            Log.d(tag, "The current count is ${it.count}")
        }
    }

    fun decreaseCount() {
        setState {
            copy(count = count - 1)
        }
    }

    class Factory @Inject constructor(): InjectableViewModelFactory<CounterViewModel, CounterViewState, CounterArgs> {
        override fun create(initialState: CounterViewState, arguments: CounterArgs?): CounterViewModel {
            return CounterViewModel(initialState, arguments)
        }
    }
}