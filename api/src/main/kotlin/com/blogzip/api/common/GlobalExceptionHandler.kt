package com.blogzip.api.common

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.notification.common.SlackSender
import com.blogzip.notification.common.SlackSender.SlackChannel.ERROR_LOG
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

private val ErrorCode.toHttpStatus: HttpStatus
    get() {
        return when (this) {
            ErrorCode.ARTICLE_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.BLOG_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.BLOG_URL_DUPLICATED -> HttpStatus.CONFLICT
            ErrorCode.EMAIL_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.LOGIN_FAILED -> HttpStatus.UNAUTHORIZED
            ErrorCode.USER_NOT_FOUND -> HttpStatus.NOT_FOUND
        }
    }

@RestControllerAdvice
class GlobalExceptionHandler(
    private val slackSender: SlackSender
) {

    val log = logger()

    @ExceptionHandler(value = [DomainException::class])
    fun handleDomainException(ex: DomainException): ResponseEntity<ErrorResponse> {
        log.warn(ex.message, ex)
        slackSender.sendStackTraceAsync(ERROR_LOG, ex)
        val errorCode: ErrorCode = ex.errorCode
        val response = ErrorResponse(code = errorCode.name, message = errorCode.message)
        return ResponseEntity
            .status(errorCode.toHttpStatus)
            .body(response)
    }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.warn(ex.message, ex)
        slackSender.sendStackTraceAsync(ERROR_LOG, ex)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    code = "ARGUMENT_NOT_VALID",
                    message = ex.bindingResult.allErrors.firstOrNull()?.defaultMessage
                )
            )
    }

    // 없는 API 호출
    @ExceptionHandler(value = [NoResourceFoundException::class])
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        val response = ErrorResponse(code = null, message = null)
        return ResponseEntity
            .internalServerError()
            .body(response)
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        slackSender.sendStackTraceAsync(ERROR_LOG, ex)
        val response = ErrorResponse(code = null, message = null)
        return ResponseEntity
            .internalServerError()
            .body(response)
    }
}