package com.weolbu.test.api

import com.weolbu.test.support.exception.WeolbuCommonErrorCode
import com.weolbu.test.support.exception.WeolbuException
import com.weolbu.test.support.exception.WeolbuException.ErrorCode.Type.BusinessException
import com.weolbu.test.support.exception.WeolbuException.ErrorCode.Type.InternalError
import com.weolbu.test.support.exception.WeolbuException.ErrorCode.Type.InvalidRequest
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
        val errorMessage = "[${e.errorCode.code}] ${e.errorCode.displayMessage} (${e.details})"

        return when (e.errorCode.type) {
            InvalidRequest -> {
                logger.info(errorMessage, e)
                ResponseEntity.badRequest().body(e.toErrorResponse())
            }

            BusinessException -> {
                logger.info(errorMessage, e)
                ResponseEntity.unprocessableEntity().body(e.toErrorResponse())
            }

            InternalError -> {
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

        val errorResponse: ErrorResponse = e.toErrorResponse(WeolbuCommonErrorCode.BadRequest)
        return ResponseEntity.internalServerError().body(errorResponse)
    }

    @ExceptionHandler(Throwable::class)
    fun fallback(t: Throwable): ResponseEntity<ErrorResponse> {
        logger.error("Unknown Exception", t)

        val errorResponse: ErrorResponse = t.toErrorResponse(WeolbuCommonErrorCode.InternalServerError)
        return ResponseEntity.internalServerError().body(errorResponse)
    }

    private fun WeolbuException.toErrorResponse(): ErrorResponse {
        return ErrorResponse(
            code = this.errorCode.code,
            displayMessage = this.errorCode.displayMessage,
            details = this.details,
        )
    }

    private fun Throwable.toErrorResponse(errorCode: WeolbuException.ErrorCode): ErrorResponse {
        return ErrorResponse(
            code = errorCode.code,
            displayMessage = errorCode.displayMessage,
            details = this.message ?: this.toString(),
        )
    }
}
