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
) {
  var log = logger()

  fun sendNewArticles(to: User, articles: List<Article>) {
    val content = emailTemplateParser.parseArticles(to, articles)
    sendEmail(to.email, "구독한 블로그의 새 글", content)
  }

  fun sendEmail(to: String, subject: String, content: String) {
    try {
      val mimeMessage = mailSender.createMimeMessage()
      val helper = MimeMessageHelper(mimeMessage, "UTF-8")
      helper.setTo(to)
      helper.setSubject(subject)
      helper.setText(content, true)
      helper.setFrom(InternetAddress(senderEmailAddress, senderName).toString())
      mailSender.send(mimeMessage)
    } catch (e: Exception) {
      log.error("이메일 발송 실패. to=${to}", e)
      slackSender.sendStackTraceAsync(ERROR_LOG, e)
    }
  }
}
