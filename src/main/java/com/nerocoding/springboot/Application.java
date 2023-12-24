package com.nerocoding.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//@EnableJpaAuditing			// JPA Auditing
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
//		SpringApplication.run(Application.class, args);
		SpringApplication application = new SpringApplication(Application.class);
		application.addListeners(new ApplicationPidFileWriter());
		application.run(args);
	}
}
