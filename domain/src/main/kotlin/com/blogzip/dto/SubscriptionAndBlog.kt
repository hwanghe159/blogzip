package com.blogzip.dto

import com.blogzip.domain.Blog
import com.blogzip.domain.Subscription

data class SubscriptionAndBlog(
    val subscription: Subscription,
    val blog: Blog,
) {
}