package com.blogzip.batch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
@EnableBatchProcessing
@EntityScan(basePackages = ["com.blogzip.domain"])
@ComponentScan(basePackages = ["com.blogzip.domain"])
class BatchTestConfig {

  @Bean
  @Primary
  fun dataSource(): DataSource {
    return EmbeddedDatabaseBuilder()
      .setType(EmbeddedDatabaseType.H2)
      .addScript("/org/springframework/batch/core/schema-h2.sql")
      .addScript("/schema-h2.sql")
      .build()
  }

  @Bean
  fun transactionManager(): PlatformTransactionManager {
    return DataSourceTransactionManager(dataSource())
  }
}