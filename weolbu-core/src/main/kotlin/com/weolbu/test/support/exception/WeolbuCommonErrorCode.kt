package com.weolbu.test.support.exception

import com.weolbu.test.support.exception.WeolbuException.ErrorCode.Type

object WeolbuCommonErrorCode {
    val InternalServerError = WeolbuException.ErrorCode(
        code = "WLB0000",
        displayMessage = "일시적인 에러가 발생했어요. 잠시 후 다시 시도해 주세요.",
        type = Type.InternalError,
    )

    val BadRequest = WeolbuException.ErrorCode(
        code = "WLB0001",
        displayMessage = "API 요청이 잘못 되었어요",
        type = Type.InvalidRequest,
    )
}
