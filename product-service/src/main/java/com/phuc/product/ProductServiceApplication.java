package com.phuc.product;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductServiceApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.ignoreIfMissing()
				.load();
		setSpringProperties(dotenv);
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	private static void setSpringProperties(Dotenv dotenv) {
		System.setProperty("MONGO_HOST", dotenv.get("MONGO_HOST", ""));
		System.setProperty("MONGO_USERNAME", dotenv.get("MONGO_USERNAME", ""));
		System.setProperty("MONGO_PASSWORD", dotenv.get("MONGO_PASSWORD", ""));

		System.setProperty("AUTH0_DOMAIN", dotenv.get("AUTH0_DOMAIN", ""));

	}
}
