package com.weolbu.test.support.exception

abstract class WeolbuException(
    override val cause: Throwable? = null,
) : RuntimeException(cause) {
    abstract val errorCode: WeolbuErrorCode
    override val message: String
        get() = "[${errorCode.code}] ${errorCode.displayMessage} (${errorCode.details})"
}
