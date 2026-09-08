package io.playground.orderservice.infrastructure.util

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.playground.orderservice.exception.BusinessErrorCode
import io.playground.orderservice.exception.BusinessException
import org.springframework.stereotype.Component

@Component
class JsonUtil(
    private val objectMapper: ObjectMapper,
) {
    fun toJson(obj: Any?): String {
        try {
            return objectMapper.writeValueAsString(obj)
        } catch (_: JsonProcessingException) {
            throw BusinessException(BusinessErrorCode.JSON_PROCESSING_FAILED)
        }
    }

    fun <T> fromJson(json: String, type: Class<T>): T {
        try {
            return objectMapper.readValue(json, type)
        } catch (_: JsonProcessingException) {
            throw BusinessException(BusinessErrorCode.JSON_PROCESSING_FAILED)
        }
    }

    fun <T> convert(fromValue: Any, typeReference: TypeReference<T>): T {
        try {
            return objectMapper.convertValue(fromValue, typeReference)
        } catch (_: IllegalArgumentException) {
            throw BusinessException(BusinessErrorCode.JSON_PROCESSING_FAILED)
        }
    }

    fun isBusinessDetailError(json: String): Boolean {
        return try {
            objectMapper.readValue(json, ErrorBody::class.java).detail != null
        } catch (_: JsonProcessingException) {
            false
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class ErrorBody(
        val detail: String? = null,
    )
}
