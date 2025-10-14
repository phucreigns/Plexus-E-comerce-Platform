package com.phuc.review;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.phuc.review.httpclient")
public class ReviewServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.ignoreIfMissing()
				.load();
		setSpringProperties(dotenv);
		SpringApplication.run(ReviewServiceApplication.class, args);
	}

	private static void setSpringProperties(Dotenv dotenv) {
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST", ""));
		System.setProperty("POSTGRES_USERNAME", dotenv.get("POSTGRES_USERNAME", ""));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD", ""));
		System.setProperty("PRODUCT_SERVICE_URL", dotenv.get("PRODUCT_SERVICE_URL", ""));
		System.setProperty("ORDER_SERVICE_URL", dotenv.get("ORDER_SERVICE_URL", ""));
		System.setProperty("SHOP_SERVICE_URL", dotenv.get("SHOP_SERVICE_URL", ""));
		System.setProperty("FILE_SERVICE_URL", dotenv.get("FILE_SERVICE_URL", ""));
		
		System.setProperty("AUTH0_DOMAIN", dotenv.get("AUTH0_DOMAIN", ""));
	}
}
