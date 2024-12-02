package com.blogzip.ai.summary

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.stereotype.Component

@Component
class SingleRequestGenerator {

    private val objectMapper = jsonMapper { addModule(kotlinModule()) }
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    fun generate(customId: String, content: String, model: String): SingleRequest {
        val json = """
        {
           "custom_id":"$customId",
           "method":"POST",
           "url":"/v1/chat/completions",
           "body":{
              "model":"$model",
              "messages":[
                 {
                    "role":"system",
                    "content":"한국인을 대상으로 하는 테크블로그 내용 요약기 및 키워드 추출기"
                 },
                 {
                    "role":"user",
                    "content":${objectMapper.writeValueAsString(content)}
                 }
              ],
              "response_format":{
                 "type":"json_schema",
                 "json_schema":{
                    "name":"summary_and_keywords",
                    "strict":true,
                    "schema":{
                       "type":"object",
                       "properties":{
                          "summary":{
                             "type":"string",
                             "description":"당신은 마크다운 형식의 텍스트를 5~8줄 정도의 간결한 요약문으로 변환하는 전문가입니다. 요약 시 다음 지침을 따르세요:\n\n1. 친근하면서도 정중한 '~~요'체의 존댓말을 사용하세요.\n2. 요약 내용만을 직접적으로 제시하세요.\n3. 반말이나 명사로 끝나는 문장은 피하세요.\n4. 인사말, 자기소개, 블로그 소개 등은 생략하세요.\n5. \"네\", \"알겠어요\", \"요약해드릴게요\" 등의 불필요한 표현은 사용하지 마세요.\n6. 대화형 AI가 아닌 요약 전문가로서의 역할에 충실하세요.\n7. 응답은 마크다운 형식이 아닌 줄글로 응답하고, 절대 8줄을 넘기지 마세요.\n\n입력된 마크다운 텍스트의 핵심 내용을 정확하고 간결하게 전달하는 데 집중하세요. 요약문은 독자가 원문의 주요 내용을 빠르게 파악할 수 있도록 작성되어야 합니다."
                          },
                          "keywords":{
                             "type":"array",
                             "description":"게시물과 관련된 키워드 목록입니다. 이 글의 분야, 대주제, 기술명 등이 될 수 있습니다. 예를 들어 '백엔드','프론트엔드','AI','DevOps' 같이 분야가 될 수도 있고, '소프트스킬','자기개발' 등 주제가 될 수도 있습니다. 또는 'MySQL','Redis','Kafka','Spring' 같은 기술명이 될 수도 있습니다.",
                             "items":{
                                "type":"string"
                             }
                          }
                       },
                       "required":[
                          "summary",
                          "keywords"
                       ],
                       "additionalProperties":false
                    }
                 }
              }
           }
        }      
        """.trimIndent()

        return objectMapper.readValue(json, SingleRequest::class.java)
    }
}
