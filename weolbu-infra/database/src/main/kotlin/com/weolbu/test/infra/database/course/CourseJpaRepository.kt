package com.weolbu.test.infra.database.course

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseJpaRepository : JpaRepository<CourseEntity, Long>, CourseJpaCustomRepository
