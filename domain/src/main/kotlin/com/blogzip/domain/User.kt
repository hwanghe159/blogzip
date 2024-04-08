package com.blogzip.domain

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.DayOfWeek
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val email: String,

    val password: String? = null,

    var verificationCode: String,

    var isVerified: Boolean = false,

    @Convert(converter = ReceiveDaysConverter::class)
    val receiveDays: List<DayOfWeek> = DayOfWeek.entries.toList(),

    @OneToMany(mappedBy = "user")
    val subscriptions: List<Subscription> = emptyList(),

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
}