package com.blogzip.batch.summarize

import com.blogzip.batch.common.getParameter
import com.blogzip.batch.summarize.SummarizeJobConfig.Companion.PARAMETER_NAME
import com.blogzip.crawler.common.logger
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SummarizeTasklet(
    private val articleContentSummarizeService: ArticleContentSummarizeService,
) : Tasklet {

    val log = logger()

    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext
    ): RepeatStatus? {
        val parameter = chunkContext.getParameter(PARAMETER_NAME)
        val startDate: LocalDate
        if (parameter.isNullOrBlank()) {
            val yesterday = LocalDate.now().minusDays(1)
            startDate = yesterday
        } else {
            startDate = LocalDate.parse(parameter)
        }
        articleContentSummarizeService.summarize(startDate = startDate)
        return RepeatStatus.FINISHED
    }
}