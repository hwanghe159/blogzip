package com.blogzip.api.common

import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

@Component
class ImageService(private val s3Client: S3Client) {

    // todo 이미지 조회 / 업로드 기능
    fun upload() {

    }

    fun get() {
        s3Client.getObjectAsBytes(GetObjectRequest.builder()
            .build())
    }
}