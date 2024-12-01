package com.weolbu.test.infra.database.course

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.weolbu.test.course.domain.CourseSort
import com.weolbu.test.course.domain.CourseSort.HIGHEST_APPLICATION_RATE
import com.weolbu.test.course.domain.CourseSort.MOST_APPLICANTS
import com.weolbu.test.course.domain.CourseSort.RECENTLY_REGISTERED
import com.weolbu.test.infra.database.course.QCourseEntity.courseEntity
import com.weolbu.test.infra.database.course.QCourseRegistrationEntity.courseRegistrationEntity
import com.weolbu.test.support.data.OffsetPageContent
import com.weolbu.test.support.data.OffsetPageRequest

interface CourseJpaCustomRepository {
    fun getAllCourses(pageRequest: OffsetPageRequest, sort: CourseSort): OffsetPageContent<CourseAndRegistrationEntity>
}

class CourseJpaCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CourseJpaCustomRepository {
    override fun getAllCourses(
        pageRequest: OffsetPageRequest,
        sort: CourseSort,
    ): OffsetPageContent<CourseAndRegistrationEntity> {
        val content: List<CourseAndRegistrationEntity> = queryFactory.select(
            QCourseAndRegistrationEntity(
                courseEntity,
                courseRegistrationEntity.count(),
                registrationRate,
            ),
        ).from(courseEntity)
            .leftJoin(courseRegistrationEntity)
            .on(courseEntity.id.eq(courseRegistrationEntity.courseId))
            .groupBy(courseEntity)
            .limit(pageRequest.pageSize.toLong())
            .offset(pageRequest.offset().toLong())
            .orderBy(
                when (sort) {
                    RECENTLY_REGISTERED -> courseEntity.createdAt.desc()
                    MOST_APPLICANTS -> courseRegistrationEntity.count().desc()
                    HIGHEST_APPLICATION_RATE -> registrationRate.desc()
                },
            ).fetch()

        return OffsetPageContent(
            pageNum = pageRequest.pageNum,
            pageSize = pageRequest.pageSize,
            totalElements = getCourseCount().toInt(),
            items = content,
        )
    }

    private fun getCourseCount(): Long {
        return queryFactory
            .select(courseEntity.count())
            .from(courseEntity)
            .fetchOne() ?: 0L
    }

    private val registrationRate: NumberExpression<Double> =
        Expressions.asNumber(
            courseRegistrationEntity.id.count().castToNum(Double::class.java)
                .divide(courseEntity.maxParticipants.castToNum(Double::class.java))
                .multiply(100.0),
        ).castToNum(Double::class.java)
}
