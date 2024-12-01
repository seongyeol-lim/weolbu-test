package com.weolbu.test.infra.database

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableJpaRepositories
class WeolbuJpaConfiguration
