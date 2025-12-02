package com.phuc.order;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.phuc.order", "com.phuc.order.mapper"})
public class OrderServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.ignoreIfMissing()
				.load();
		setSpringProperties(dotenv);
		SpringApplication.run(OrderServiceApplication.class, args);
	}

	private static void setSpringProperties(Dotenv dotenv) {
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST", "localhost"));
		System.setProperty("POSTGRES_PORT", dotenv.get("POSTGRES_PORT", "5432"));
		System.setProperty("POSTGRES_USERNAME", dotenv.get("POSTGRES_USERNAME", ""));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD", ""));
		System.setProperty("ORDER_SERVICE_DB", dotenv.get("ORDER_SERVICE_DB", "OrderService"));
		
		System.setProperty("PRODUCT_SERVICE_URL", dotenv.get("PRODUCT_SERVICE_URL", "http://localhost:8091/product"));
		System.setProperty("PAYMENT_SERVICE_URL", dotenv.get("PAYMENT_SERVICE_URL", "http://localhost:8099/payment"));

		System.setProperty("AUTH0_DOMAIN", dotenv.get("AUTH0_DOMAIN", ""));
	}
}
