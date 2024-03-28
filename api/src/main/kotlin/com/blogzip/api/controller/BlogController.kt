package com.blogzip.api.controller

import com.blogzip.api.dto.BlogResponse
import com.blogzip.domain.BlogRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class BlogController(
//    private val blogRepository: BlogRepository
) {

    @GetMapping("/api/v1/blog/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<BlogResponse> {
//        val blog = blogRepository.findById(id)
//            .map {
//                BlogResponse(
//                    id = it.id!!,
//                    name = it.name,
//                    url = it.url,
//                    createdBy = it.createdBy.id!!,
//                    createdAt = it.createdAt,
//                )
//            }
//            .orElseThrow()
        val blog = null
        return ResponseEntity.ok(blog)
    }
}