package com.phuc.notification;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.ignoreIfMissing()
				.load();
		setSpringProperties(dotenv);
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	private static void setSpringProperties(Dotenv dotenv) {
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST", ""));
		System.setProperty("POSTGRES_USERNAME", dotenv.get("POSTGRES_USERNAME", ""));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD", ""));
		System.setProperty("AUTH0_DOMAIN", dotenv.get("AUTH0_DOMAIN", ""));
		
		// Email configuration
		System.setProperty("MAIL_HOST", dotenv.get("MAIL_HOST", "smtp.gmail.com"));
		System.setProperty("MAIL_PORT", dotenv.get("MAIL_PORT", "587"));
		System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME", ""));
		System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD", ""));
	}
}
