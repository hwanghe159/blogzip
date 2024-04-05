package com.blogzip.notification.email

import org.springframework.stereotype.Component
import software.amazon.awssdk.regions.Region.AP_NORTHEAST_2
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*


@Component
class EmailSender(
    private val emailTemplateParser: EmailTemplateParser,
) {
    companion object {
        private const val SENDER_EMAIL_ADDRESS = "no-replay@blogzip.com"
    }

    fun sendNewArticles(to: String, articles: List<Article>) {
        if (articles.isEmpty()) {
            return
        }
        val content = emailTemplateParser.parseArticles(articles)
        sendEmailUsingSES(to, "제목", content)
    }

    // todo 요약 퀄리티 높이기
    // todo api, batch EC2 배포, 배치 트리거링

    fun sendVerification(to: String, code: String) {
        val content = emailTemplateParser.parseVerification(to, code)
        sendEmailUsingSES(to, "제목", content)
    }

    private fun sendEmailUsingSES(to: String, subject: String, content: String) {
        val sesClient = SesClient.builder()
            .region(AP_NORTHEAST_2)
            .build()
        sesClient.sendEmail(
            SendEmailRequest.builder()
                .source(SENDER_EMAIL_ADDRESS)
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
        sesClient.close()
    }
}