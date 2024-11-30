package com.weolbu.test.support.exception

abstract class WeolbuException(
    override val cause: Throwable? = null,
) : RuntimeException(cause) {
    abstract val errorCode: ErrorCode
    abstract val details: String

    override val message: String
        get() = details

    data class ErrorCode(
        /** client-side 분기 위한 에러코드 */
        val code: String,

        /** 사용자에게 노출되는 에러 메시지 */
        val displayMessage: String,

        /** 에러코드 유형 */
        val type: Type,
    ) {
        enum class Type {
            /** 잘못된 요청 */
            InvalidRequest,

            /** 비지니스 로직 예외 (HttpStatusCode: 422) */
            BusinessException,

            /** 서버 내부 에러 (HttpStatusCode: 500) */
            InternalError,
        }
    }
}
