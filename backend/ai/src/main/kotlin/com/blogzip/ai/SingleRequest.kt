package com.blogzip.ai

data class SingleRequest(
    val customId: String,
    val method: String,
    val url: String,
    val body: Body
) {
    data class Body(
        val model: String,
        val messages: List<Message>,
        val responseFormat: ResponseFormat,
    ) {
        data class Message(
            val role: String,
            val content: String
        )

        data class ResponseFormat(
            val type: String,
            val jsonSchema: JsonSchema
        ) {
            data class JsonSchema(
                val name: String,
                val strict: Boolean,
                val schema: Schema
            ) {
                data class Schema(
                    val type: String,
                    val properties: Properties,
                    val required: List<String>,
                    val additionalProperties: Boolean
                ) {
                    data class Properties(
                        val summary: Summary,
                        val keywords: Keywords
                    ) {
                        data class Summary(
                            val type: String,
                            val description: String,
                        )

                        data class Keywords(
                            val type: String,
                            val description: String,
                            val items: Items,
                        ) {
                            data class Items(
                                val type: String,
                            )
                        }
                    }
                }
            }
        }
    }
}
