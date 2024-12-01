package com.weolbu.test.adapter

import com.weolbu.test.infra.database.WeolbuJpaConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan
@Import(WeolbuJpaConfiguration::class)
class WeolbuAdapterConfiguration
