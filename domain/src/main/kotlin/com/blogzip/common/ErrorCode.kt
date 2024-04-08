package com.blogzip.common

enum class ErrorCode(val message: String) {
    BLOG_NOT_FOUND("id로 블로그를 찾을 수 없음"),
    EMAIL_NOT_FOUND("이메일로 회원을 찾을 수 없음"),
    ALREADY_VERIFIED("이미 인증이 완료된 이메일 주소 입니다."),
    ALREADY_SENT_VERIFICATION_EMAIL("인증 메일을 보낸 이력이 있습니다. 메일함을 확인해 주세요!"),
    VERIFY_FAILED("인증코드가 다르거나 만료됨"),
}