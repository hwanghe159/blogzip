package com.blogzip.batch.common

import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.MONITORING
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@JobScope
@Component
class JobResultNotifier(
  private val slackSender: SlackSender,
  private val environment: Environment,
) : JobExecutionListener {

  override fun beforeJob(jobExecution: JobExecution) {
    slackSender.sendMessageAsync(
      channel = MONITORING,
      message = """
                batch(${jobExecution.jobInstance.jobName}) 시작
                profile = ${environment.activeProfiles.joinToString(",")}
            """.trimIndent()
    )
  }

  override fun afterJob(jobExecution: JobExecution) {
    slackSender.sendMessageAsync(
      channel = MONITORING,
      message = """
                batch(${jobExecution.jobInstance.jobName}) 종료. 코드 = ${jobExecution.exitStatus.exitCode}
                profile = ${environment.activeProfiles.joinToString(",")}
            """.trimIndent()
    )
  }
}