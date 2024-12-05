package com.weolbu.test.api

import com.weolbu.test.support.exception.WeolbuErrorCode
import com.weolbu.test.support.exception.WeolbuErrorCode.Type
import com.weolbu.test.support.exception.WeolbuException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

data class ErrorResponse(
    val code: String,
    val displayMessage: String,
    val details: String,
)

@RestControllerAdvice
class ExceptionHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(WeolbuException::class)
    fun handle(e: WeolbuException): ResponseEntity<ErrorResponse> {
        val errorMessage = e.message
        return when (e.errorCode.type) {
            Type.InvalidRequest -> {
                logger.info(errorMessage, e)
                ResponseEntity.badRequest().body(e.toErrorResponse())
            }

            Type.BusinessException -> {
                logger.info(errorMessage, e)
                ResponseEntity.unprocessableEntity().body(e.toErrorResponse())
            }

            Type.InternalError -> {
                logger.error(errorMessage, e)
                ResponseEntity.internalServerError().body(e.toErrorResponse())
            }
        }
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class,
    )
    fun handleMethodArgumentTypeMismatchException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.info("API 요청이 잘못 되었어요", e)

        val errorCode = WeolbuErrorCode.badRequest(e.message ?: e.toString())
        return ResponseEntity.internalServerError().body(errorCode.toErrorResponse())
    }

    @ExceptionHandler(Throwable::class)
    fun fallback(t: Throwable): ResponseEntity<ErrorResponse> {
        logger.error("Unknown Exception", t)

        val errorCode = WeolbuErrorCode.internalError(t.message ?: t.toString())
        return ResponseEntity.internalServerError().body(errorCode.toErrorResponse())
    }

    private fun WeolbuErrorCode.toErrorResponse(): ErrorResponse {
        return ErrorResponse(code = this.code, displayMessage = this.displayMessage, details = this.details)
    }

    private fun WeolbuException.toErrorResponse(): ErrorResponse {
        return this.errorCode.toErrorResponse()
    }
}
