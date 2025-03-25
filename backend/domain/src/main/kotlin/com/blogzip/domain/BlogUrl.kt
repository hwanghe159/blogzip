package com.blogzip.domain

import java.net.URI
import java.net.URISyntaxException

data class BlogUrl private constructor(
  private val uri: URI
) {

  companion object {

    /**
     * @throws URISyntaxException
     */
    fun from(str: String): BlogUrl {
      val initialUri = if (!str.startsWith("http://") && !str.startsWith("https://")) {
        URI("https://$str")
      } else {
        URI(str)
      }
      val scheme = initialUri.scheme ?: "https"
      val host = initialUri.host.replace("www.", "")
      val port = initialUri.port
      val path = if (initialUri.path == "/") "" else initialUri.path // todo /로 끝나는 경우도 핸들링
      val normalizedUri = URI(scheme, null, host, port, path, null, null)
      return BlogUrl(normalizedUri.normalize())
    }
  }

  override fun toString(): String {
    return "${uri.scheme}://${uri.host}${uri.path}"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return this.toString() == (other as BlogUrl).toString()
  }

  override fun hashCode(): Int {
    return toString().hashCode()
  }
}