package com.blogzip.notification.email

import com.blogzip.logger
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.ERROR_LOG
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component


@Component
class EmailSender(
  private val mailSender: JavaMailSender,
  private val emailTemplateParser: EmailTemplateParser,
  private val slackSender: SlackSender,
  @Value("\${app.mail.from-name:blogzip}") private val senderName: String,
  @Value("\${app.mail.from-address:no-reply@blogzip.co.kr}") private val senderEmailAddress: String,
  @Value("\${spring.mail.host:}") private val smtpHost: String,
  @Value("\${spring.mail.username:}") private val smtpUsername: String,
  @Value("\${spring.mail.password:}") private val smtpPassword: String,
) {
  var log = logger()

  fun sendNewArticles(to: User, articles: List<Article>) {
    val content = emailTemplateParser.parseArticles(to, articles)
    val result = sendEmail(to.email, "구독한 블로그의 새 글", content)
    if (!result.success) {
      log.warn("구독 메일 발송 실패. to={}, reason={}", to.email, result.message)
    }
  }

  fun sendEmail(to: String, subject: String, content: String): EmailSendResult {
    validateSmtpConfig()?.let { reason ->
      log.error("이메일 발송 실패. to={}, reason={}", to, reason)
      return EmailSendResult(success = false, message = reason)
    }

    try {
      val mimeMessage = mailSender.createMimeMessage()
      val helper = MimeMessageHelper(mimeMessage, "UTF-8")
      helper.setTo(to)
      helper.setSubject(subject)
      helper.setText(content, true)
      helper.setFrom(InternetAddress(senderEmailAddress, senderName).toString())
      mailSender.send(mimeMessage)
      return EmailSendResult(success = true, message = "메일 발송 성공")
    } catch (e: Exception) {
      log.error("이메일 발송 실패. to=${to}", e)
      slackSender.sendStackTraceAsync(ERROR_LOG, e)
      return EmailSendResult(success = false, message = e.message ?: e::class.simpleName ?: "Unknown error")
    }
  }

  private fun validateSmtpConfig(): String? {
    val missing = mutableListOf<String>()
    if (smtpHost.isBlank()) missing += "OCI_EMAIL_SMTP_HOST"
    if (smtpUsername.isBlank()) missing += "OCI_EMAIL_SMTP_USERNAME"
    if (smtpPassword.isBlank()) missing += "OCI_EMAIL_SMTP_PASSWORD"
    if (missing.isEmpty()) return null
    return "SMTP 설정 누락: ${missing.joinToString(", ")}"
  }

  data class EmailSendResult(
    val success: Boolean,
    val message: String,
  )
}
