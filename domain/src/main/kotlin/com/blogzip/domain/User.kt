package com.blogzip.domain

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
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

    val googleId: String? = null,

    var verificationCode: String,

    var isVerified: Boolean = false,

    val receiveDays: String,

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

    var verificationCodeExpiredAt: LocalDateTime =
        LocalDateTime.now().plusHours(VERIFICATION_CODE_EXPIRY_HOURS)
) {

    companion object {
        private const val VERIFICATION_CODE_EXPIRY_HOURS: Long = 24
    }

    fun verify(verificationCode: String): User {
        if (this.isVerified) {
            throw DomainException(ErrorCode.ALREADY_VERIFIED)
        }
        if (this.verificationCode != verificationCode || isVerificationCodeExpired()) {
            throw DomainException(ErrorCode.VERIFY_FAILED)
        }
        this.isVerified = true
        return this
    }

    fun isVerificationCodeExpired(): Boolean {
        return this.verificationCodeExpiredAt <= LocalDateTime.now()
    }

    fun refreshVerificationCode(verificationCode: String): User {
        this.verificationCode = verificationCode
        this.verificationCodeExpiredAt =
            LocalDateTime.now().plusHours(VERIFICATION_CODE_EXPIRY_HOURS)
        return this
    }

    fun addSubscription(blog: Blog): Subscription {
        val subscription = Subscription(user = this, blog = blog)
        this.subscriptions.add(subscription)
        return subscription
    }

    fun deleteSubscription(blogId: Long): Boolean {
        return this.subscriptions.removeIf { it.blog.id == blogId }
    }

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
}