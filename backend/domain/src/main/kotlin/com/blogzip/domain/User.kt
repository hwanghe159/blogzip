package com.blogzip.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class User(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val email: String,

  @Enumerated(EnumType.STRING)
  var socialType: SocialType,

  var socialId: String,

  var receiveDays: String,

  @OneToMany(
    mappedBy = "user",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true
  )
  val subscriptions: MutableList<Subscription> = mutableListOf(),

  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.MIN,

  @LastModifiedDate
  var updatedAt: LocalDateTime = LocalDateTime.MIN,
) {

  fun getAccumulatedDates(emailDate: LocalDate): List<LocalDate> {
    val receiveDays = ReceiveDaysConverter.toList(this.receiveDays)
    if (!receiveDays.contains(emailDate.dayOfWeek)) {
      return emptyList()
    }
    val result = mutableListOf<LocalDate>()
    var date = emailDate
    while (true) {
      date = date.minusDays(1)
      if (receiveDays.contains(date.dayOfWeek)) {
        result.add(date)
        break
      }
      result.add(date)
    }
    return result.reversed()
  }

  fun getAllSubscribingBlogIds(): Set<Long> {
    return this.subscriptions.map { it.blogId }.toSet()
  }

  fun addSubscription(blogId: Long): Subscription {
    val existingSubscription = this.subscriptions.firstOrNull { it.blogId == blogId }
    if (existingSubscription == null) {
      val subscription = Subscription(user = this, blogId = blogId)
      this.subscriptions.add(subscription)
      return subscription
    } else {
      return existingSubscription
    }
  }

  fun deleteSubscription(blogId: Long): Boolean {
    return this.subscriptions.removeIf { it.blogId == blogId }
  }

  fun updateReceiveDays(receiveDays: String): User {
    this.receiveDays = receiveDays
    return this
  }

  fun updateGoogleId(googleId: String) {
    this.socialType = SocialType.GOOGLE
    this.socialId = googleId
  }
}
