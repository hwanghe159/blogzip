package com.blogzip.api.config

import com.blogzip.crawler.config.WebDriverConfig
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component

@Component
class ShutdownEventListener(
    private val webDriverConfig: WebDriverConfig,
) : ApplicationListener<ContextClosedEvent> {

    override fun onApplicationEvent(event: ContextClosedEvent) {
        webDriverConfig.quitWebDriver()
    }
}