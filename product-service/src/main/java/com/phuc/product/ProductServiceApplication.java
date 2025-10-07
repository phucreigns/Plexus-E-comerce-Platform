package com.phuc.product;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
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
        System.setProperty("SHOP_SERVICE_URL", dotenv.get("SHOP_SERVICE_URL", ""));
        System.setProperty("FILE_SERVICE_URL", dotenv.get("FILE_SERVICE_URL", ""));
        System.setProperty("ORDER_SERVICE_URL", dotenv.get("ORDER_SERVICE_URL", ""));
		
        System.setProperty("AUTH0_DOMAIN", dotenv.get("AUTH0_DOMAIN", ""));

	}
}
