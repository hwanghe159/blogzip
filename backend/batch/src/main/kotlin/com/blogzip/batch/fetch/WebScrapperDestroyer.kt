package com.blogzip.batch.fetch

import com.blogzip.crawler.service.WebScrapper
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.stereotype.Component

// WebScrapper 가 @StepScope 이기 때문에 StepExecutionListener 를 사용하여 endUse()를 호출한다.
@Component
class WebScrapperDestroyer(
    private val webScrapper: WebScrapper,
) : StepExecutionListener {

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        webScrapper.endUse()
        return stepExecution.exitStatus
    }
}