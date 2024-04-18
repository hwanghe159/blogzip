package com.blogzip.batch.fetch

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@SpringBatchTest
class FetchNewArticlesJobConfigTest {

    @Autowired
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var fetchNewArticlesJob: Job

    @Test
    @Tag("exclude")
    fun fetchNewArticlesJob() {
        jobLauncherTestUtils.job = fetchNewArticlesJob
        val jobExecution = jobLauncherTestUtils.launchJob()
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo("COMPLETED")
    }
}