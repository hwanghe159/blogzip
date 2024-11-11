package com.blogzip.ai

data class BatchResponse(
    val id: String?,
    val customId: String?,
    val response: ResponseDetails?,
    val error: ErrorDetails?,
) {
    data class ResponseDetails(
        val statusCode: Int?,
        val requestId: String?,
        val body: ResponseBody?,
    )

    data class ResponseBody(
        val id: String?,
        val `object`: String?,
        val created: Long?,
        val model: String?,
        val choices: List<Choice>?,
        val usage: Usage?,
    ) {
        data class Choice(
            val index: Int?,
            val message: Message?,
        ) {
            data class Message(
                val role: String?,
                val content: String?,
            )
        }

        data class Usage(
            val promptTokens: Int?,
            val completionTokens: Int?,
            val totalTokens: Int?,
        )
    }

    data class ErrorDetails(
        val message: String?,
        val type: String?,
        val param: String?,
        val code: String?,
    )
}