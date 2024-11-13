package com.blogzip.common

enum class ErrorCode(val message: String) {
    ARTICLE_NOT_FOUND("글을 찾을 수 없음"),
    BLOG_NOT_FOUND("블로그를 찾을 수 없음"),
    BLOG_URL_DUPLICATED("URL에 해당하는 블로그가 이미 존재합니다."),
    EMAIL_NOT_FOUND("이메일로 회원을 찾을 수 없음"),
    LOGIN_FAILED("로그인 실패"),
    USER_NOT_FOUND("사용자를 찾을 수 없음"),
    KEYWORD_NOT_FOUND("키워드를 찾을 수 없음"),
    KEYWORD_UPDATE_FAILED("키워드 수정 불가"),
}
