package com.blogzip.ai.summary

interface ArticleContentSummarizer {

  fun summarizeAndGetKeywordsAll(articles: List<ArticleToSummarize>): List<SummarizedArticleResult>
}