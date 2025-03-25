package com.blogzip.ai.tuning

data class FineTuningDataset(
  val messages: List<Message>
) {
  data class Message(
    val role: String,
    val content: String
  )
}