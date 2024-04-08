package com.blogzip.common

class DomainException(
    val errorCode: ErrorCode
) : RuntimeException()