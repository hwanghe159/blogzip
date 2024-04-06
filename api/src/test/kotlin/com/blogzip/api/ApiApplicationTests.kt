package com.blogzip.api

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(properties = ["spring.sql.init.mode=never"])
class ApiApplicationTests {

    @Test
    fun contextLoads() {
    }

}
