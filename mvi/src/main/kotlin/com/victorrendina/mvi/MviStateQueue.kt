package com.victorrendina.mvi

import java.util.LinkedList

internal class MviStateQueue<S : MviState> {

    private val getStateQueue = LinkedList<(state: S) -> Unit>()
    private var setStateQueue = LinkedList<S.() -> S>()

    @Synchronized
    fun enqueueGetStateBlock(block: (state: S) -> Unit) {
        getStateQueue.add(block)
    }

    @Synchronized
    fun enqueueSetStateBlock(block: S.() -> S) {
        setStateQueue.add(block)
    }

    @Synchronized
    fun dequeueGetStateBlock(): ((state: S) -> Unit)? {
        if (getStateQueue.isEmpty()) return null

        return getStateQueue.removeFirst()
    }

    @Synchronized
    fun dequeueAllSetStateBlocks(): List<(S.() -> S)>? {
        // do not allocate empty queue for no-op flushes
        if (setStateQueue.isEmpty()) return null

        val queue = setStateQueue
        setStateQueue = LinkedList()
        return queue
    }
}