package com.blogzip.api.dto.admin

import com.blogzip.domain.Article
import com.blogzip.domain.Keyword
import java.time.LocalDate

data class ArticleResponse private constructor(
    val id: Long,
    val blogId: Long,
    val title: String,
    val content: String,
    val url: String,
    val summary: String?,
    val summarizedBy: String?,
    val createdDate: LocalDate?,
    val keywords: List<HeadKeywordResponse>,
) {
    companion object {
        fun from(article: Article, keywords: List<Keyword>): ArticleResponse {
//            val headKeywords = keywords
//                .filter { it.head != null }
//                .groupBy { it.head!! }
//                .map { entry ->
//                    val head = entry.key
//                    val followers = entry.value
//                    HeadKeywordResponse(
//                        id = head.id!!,
//                        value = head.value,
//                        followers = followers.map {
//                            FollowerKeywordResponse(
//                                id = it.id!!,
//                                value = it.value,
//                                createdAt = it.createdAt,
//                            )
//                        },
//                        createdAt = head.createdAt,
//                    )
//                }
            val headKeywords = keywords
                .filter { it.isHead() }
                .map {
                    HeadKeywordResponse(
                        id = it.id!!,
                        value = it.value,
                        followers = it.followers.map {
                            FollowerKeywordResponse(
                                id = it.id!!,
                                value = it.value,
                                createdAt = it.createdAt,
                            )
                        },
                        createdAt = it.createdAt,
                    )
                }

            return ArticleResponse(
                id = article.id!!,
                blogId = article.blogId,
                title = article.title,
                content = article.content,
                url = article.url,
                summary = article.summary,
                summarizedBy = article.summarizedBy,
                createdDate = article.createdDate,
                keywords = headKeywords
            )
        }
    }
}