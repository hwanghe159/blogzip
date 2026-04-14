package com.blogzip.common

enum class ErrorCode(val message: String) {
  ARTICLE_NOT_FOUND("글을 찾을 수 없음"),
  ARTICLE_REPORT_NOT_FOUND("글 신고를 찾을 수 없음"),
  ARTICLE_SUMMARY_NOT_FOUND("글 요약 이력을 찾을 수 없음"),
  BLOG_NOT_FOUND("블로그를 찾을 수 없음"),
  BLOG_URL_DUPLICATED("URL에 해당하는 블로그가 이미 존재합니다."),
  BLOG_URL_NOT_VALID("URL이 올바르지 않습니다."),
  BLOG_METADATA_FETCH_FAILED("블로그 메타데이터 조회에 실패했습니다."),
  EMAIL_NOT_FOUND("이메일로 회원을 찾을 수 없음"),
  LOGIN_FAILED("로그인 실패"),
  USER_NOT_FOUND("사용자를 찾을 수 없음"),
  USER_WITHDRAWN("탈퇴한 사용자입니다."),
  KEYWORD_NOT_FOUND("키워드를 찾을 수 없음"),
  KEYWORD_UPDATE_FAILED("키워드 수정 불가"),
}
