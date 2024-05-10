package com.blogzip.notification.email

import com.blogzip.notification.common.logger
import com.blogzip.notification.config.AwsSesProperties
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region.AP_NORTHEAST_2
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*


@Component
class EmailSender(
    private val awsSesProperties: AwsSesProperties,
    private val emailTemplateParser: EmailTemplateParser,
) {
    companion object {
        private const val SENDER_NAME = "blogzip"
        private const val SENDER_EMAIL_ADDRESS = "no-reply@blogzip.co.kr"
    }

    var log = logger()

    fun sendNewArticles(to: User, articles: List<Article>) {
        val content = emailTemplateParser.parseArticles(to, articles)
        sendEmailUsingSES(to.email, "구독한 블로그의 새 글", content)
    }

    fun sendVerification(to: String, code: String) {
        val content = emailTemplateParser.parseVerification(to, code)
        sendEmailUsingSES(to, "이메일 주소 인증", content)
    }

    private fun sendEmailUsingSES(to: String, subject: String, content: String) {
        val sesClient = SesClient.builder()
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        awsSesProperties.accessKey,
                        awsSesProperties.secretKey
                    )
                )
            )
            .region(AP_NORTHEAST_2)
            .build()
        try {
            sesClient.use { client ->
                client.sendEmail(
                    SendEmailRequest.builder()
                        .source("$SENDER_NAME <${SENDER_EMAIL_ADDRESS}>")
                        .destination(
                            Destination.builder()
                                .toAddresses(to)
                                .build()
                        )
                        .message(
                            Message.builder()
                                .subject(
                                    Content.builder()
                                        .data(subject)
                                        .build()
                                )
                                .body(
                                    Body.builder()
                                        .html(
                                            Content.builder()
                                                .data(content)
                                                .build()
                                        ).build()
                                )
                                .build()
                        )
                        .build()
                )
            }
        } catch (e: Exception) {
            log.error("이메일 발송 실패. to=${to}")
        }
    }
}