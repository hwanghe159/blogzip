package com.blogzip.api.common

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.notification.common.SlackSender
import com.blogzip.notification.common.SlackSender.SlackChannel.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val ErrorCode.toHttpStatus: HttpStatus
    get() {
        return when (this) {
            ErrorCode.BLOG_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.BLOG_URL_DUPLICATED -> HttpStatus.CONFLICT
            ErrorCode.ALREADY_VERIFIED -> HttpStatus.CONFLICT
            ErrorCode.ALREADY_SENT_VERIFICATION_EMAIL -> HttpStatus.CONFLICT
            ErrorCode.EMAIL_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.VERIFY_FAILED -> HttpStatus.UNAUTHORIZED
            ErrorCode.LOGIN_FAILED -> HttpStatus.UNAUTHORIZED
            ErrorCode.USER_NOT_FOUND -> HttpStatus.NOT_FOUND
        }
    }

@RestControllerAdvice
class GlobalExceptionHandler(
    private val SlackSender: SlackSender
) {

    val log = logger()

    @ExceptionHandler(value = [DomainException::class])
    fun handleDomainException(ex: DomainException): ResponseEntity<ErrorResponse> {
        val errorCode: ErrorCode = ex.errorCode
        val response = ErrorResponse(code = errorCode.name, message = errorCode.message)
        return ResponseEntity
            .status(errorCode.toHttpStatus)
            .body(response)
    }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "ARGUMENT_NOT_VALID",
                    message = ex.bindingResult.allErrors.firstOrNull()?.defaultMessage
                )
            )
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        SlackSender.sendStackTraceAsync(ERROR_LOG, ex)
        val response = ErrorResponse(code = null, message = null)
        return ResponseEntity
            .internalServerError()
            .body(response)
    }
}