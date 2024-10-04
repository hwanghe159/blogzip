package com.blogzip.batch.common

import org.springframework.batch.core.scope.context.ChunkContext

fun ChunkContext.getParameter(parameterName: String): String? {
    return this.stepContext.stepExecution.jobParameters.getString(parameterName)
}