package com.blogzip.batch.blog

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class RefreshBlogMetadataJobConfig(
  private val refreshBlogMetadataTasklet: RefreshBlogMetadataTasklet,
) {

  companion object {
    private const val JOB_NAME = "refresh-blog-metadata"
  }

  @Bean
  fun refreshBlogMetadataJob(
    jobRepository: JobRepository,
    platformTransactionManager: PlatformTransactionManager
  ): Job {
    return JobBuilder(JOB_NAME, jobRepository)
      .incrementer(RunIdIncrementer())
      .start(refreshBlogMetadataStep(jobRepository, platformTransactionManager))
      .build()
  }

  @JobScope
  @Bean
  fun refreshBlogMetadataStep(
    jobRepository: JobRepository,
    platformTransactionManager: PlatformTransactionManager,
  ): Step {
    return StepBuilder("refresh-blog-metadata", jobRepository)
      .tasklet(refreshBlogMetadataTasklet, platformTransactionManager)
      .allowStartIfComplete(true) // COMPLETED 상태로 끝났어도 재실행 가능
      .build()
  }
}
