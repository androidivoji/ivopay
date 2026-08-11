package com.example.ivopay.app.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object GlobalEvent {
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    suspend fun sendEvent(event: Event) {
        _events.emit(event)
    }

    sealed class Event {
        object TokenError : Event()
    }
}
