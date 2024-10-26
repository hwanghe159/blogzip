package com.blogzip.api.config

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component

@Component
class ShutdownEventListener(
) : ApplicationListener<ContextClosedEvent> {

    override fun onApplicationEvent(event: ContextClosedEvent) {
    }
}