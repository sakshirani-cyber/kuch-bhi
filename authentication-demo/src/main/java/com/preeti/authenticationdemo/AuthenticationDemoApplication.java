package com.preeti.authenticationdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.preeti.authenticationdemo.repository")
public class AuthenticationDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationDemoApplication.class, args);
	}

}
