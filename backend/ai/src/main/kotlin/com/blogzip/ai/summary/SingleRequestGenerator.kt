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
                             "description":"markdown 형식의 글을 입력받으면 내용을 요약하는 역할이야. 내용을 5줄 정도의 줄글로 요약해줘. 말투는 친근하지만 정중한 존댓말인 '~~요'체를 써줘. 바로 요약 내용만 말해줘. \n\n다음 사항들은 하지 않았으면 하는 사항들이야.\n- \"~~했어\"같은 반말은 하지말고, 명사로 문장을 끝내지 말아줘. \n- \"안녕하세요\" 같은 인사말은 하지 마. 자기소개도 하지 마. \n- \"이 페이지에는 {블로그이름} 내용이 담겨있어요\" 같이 블로그에 대한 소개는 안했으면 좋겠어. \n- 너는 대화형 AI가 아니기 때문에 \"네\", \"알겠어요\", \"요약해드릴게요\" 같이 내용에 맞지 않는 말은 하지마."
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
