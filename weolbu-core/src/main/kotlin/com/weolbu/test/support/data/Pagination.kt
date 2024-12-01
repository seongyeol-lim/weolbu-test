package com.weolbu.test.support.data

data class OffsetPageRequest private constructor(
    val pageNum: Int,
    val pageSize: Int,
) {
    fun offset(): Int = (pageNum - 1) * pageSize

    companion object {
        fun of(pageNum: Int, pageSize: Int): Result<OffsetPageRequest> {
            if (pageNum < 1 || pageSize < 1) {
                return Result.failure(IllegalArgumentException("pageNum and pageSize should be greater than or equal to 1"))
            }

            return Result.success(OffsetPageRequest(pageNum = pageNum, pageSize = pageSize))
        }

        val DEFAULT = OffsetPageRequest(pageNum = 1, pageSize = 20)
    }
}

data class OffsetPageContent<T>(
    val pageSize: Int,
    val pageNum: Int,
    val totalElements: Int,
    val items: List<T>,
)
