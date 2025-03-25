package com.blogzip.ai.summary

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * ----------------------------435837665612940111262267
 * Content-Disposition: form-data; name="$name"; filename="$originalFileName"
 * ...
 * ----------------------------435837665612940111262267
 */
class CustomMultipartFile(
  private val input: ByteArray,
  private val name: String,
  private val originalFileName: String?,
  private val contentType: String?,
) : MultipartFile {
  override fun getName(): String {
    return name
  }

  override fun getOriginalFilename(): String? {
    return originalFileName
  }

  override fun getContentType(): String? {
    return contentType
  }

  override fun isEmpty(): Boolean {
    return input.isEmpty()
  }

  override fun getSize(): Long {
    return input.size.toLong()
  }

  override fun getBytes(): ByteArray {
    return input
  }

  override fun getInputStream(): InputStream {
    return ByteArrayInputStream(input)
  }

  @Throws(IOException::class, IllegalStateException::class)
  override fun transferTo(destination: File) {
    destination.writeBytes(input)
  }
}