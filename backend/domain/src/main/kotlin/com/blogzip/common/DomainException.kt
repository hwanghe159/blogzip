package com.blogzip.common

class DomainException(val errorCode: ErrorCode) : RuntimeException(errorCode.message) {
  override val message: String
    get() = this.errorCode.message
}