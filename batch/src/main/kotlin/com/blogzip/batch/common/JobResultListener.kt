package com.blogzip.batch.common

import com.blogzip.notification.common.SlackSender
import com.blogzip.notification.common.SlackSender.SlackChannel.MONITORING
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.stereotype.Component

@JobScope
@Component
class JobResultListener(
    private val slackSender: SlackSender,
) : JobExecutionListener {

    override fun beforeJob(jobExecution: JobExecution) {
        slackSender.sendMessageAsync(
            channel = MONITORING,
            message = "batch(${jobExecution.jobInstance.jobName}) 시작."
        )
    }

    override fun afterJob(jobExecution: JobExecution) {
        slackSender.sendMessageAsync(
            channel = MONITORING,
            message = "batch(${jobExecution.jobInstance.jobName}) 종료. 상태 = ${jobExecution.status}"
        )
    }
}