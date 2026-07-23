package com.example.AuthProject;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthProject {

	public static void main(String[] args) {
		loadEnvFile();
		SpringApplication.run(AuthProject.class, args);
	}

	/**
	 * Loads backend/.env into JVM system properties before Spring starts,
	 * so placeholders like ${DB_URL} in application.properties resolve.
	 * Existing OS env vars / system properties take precedence.
	 */
	private static void loadEnvFile() {
		Dotenv dotenv = Dotenv.configure()
				.directory(System.getProperty("user.dir"))
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> {
			String key = entry.getKey();
			String value = entry.getValue();
			if (System.getenv(key) == null && System.getProperty(key) == null) {
				System.setProperty(key, value);
			}
		});
	}
}
