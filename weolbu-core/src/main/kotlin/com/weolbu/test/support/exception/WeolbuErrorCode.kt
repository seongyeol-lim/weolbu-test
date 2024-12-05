package com.weolbu.test.support.exception

import com.weolbu.test.support.exception.WeolbuErrorCode.Type

interface WeolbuErrorCode {
    val code: String
    val displayMessage: String
    val details: String
    val type: Type

    enum class Type {
        /** 잘못된 요청 */
        InvalidRequest,

        /** 비지니스 로직 예외 (HttpStatusCode: 422) */
        BusinessException,

        /** 서버 내부 에러 (HttpStatusCode: 500) */
        InternalError,
    }

    companion object {
        fun build(code: String, displayMessage: String, details: String, type: Type): WeolbuErrorCode {
            return WeolbuErrorCodeImpl(code = code, displayMessage = displayMessage, details = details, type = type)
        }

        fun internalError(details: String): WeolbuErrorCode {
            return WeolbuErrorCode(
                code = "WLB0000",
                displayMessage = "일시적인 에러가 발생했어요. 잠시 후 다시 시도해 주세요.",
                details = details,
                type = Type.InternalError,
            )
        }

        fun badRequest(details: String): WeolbuErrorCode {
            return WeolbuErrorCode(
                code = "WLB0001",
                displayMessage = "API 요청이 잘못 되었어요",
                details = details,
                type = Type.InvalidRequest,
            )
        }
    }
}

data class WeolbuErrorCodeImpl(
    override val code: String,
    override val displayMessage: String,
    override val details: String,
    override val type: Type,
) : WeolbuErrorCode

fun WeolbuErrorCode(code: String, displayMessage: String, details: String, type: Type): WeolbuErrorCode {
    return WeolbuErrorCodeImpl(code = code, displayMessage = displayMessage, details = details, type = type)
}
