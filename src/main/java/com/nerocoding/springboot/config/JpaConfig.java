package com.nerocoding.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.repository.cdi.Eager;

@Configuration
@EnableJpaAuditing          // JPA Auditing 활성화
public class JpaConfig {

}
