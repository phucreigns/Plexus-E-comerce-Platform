package com.phuc.cart;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CartServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.ignoreIfMissing()
				.load();
		setSpringProperties(dotenv);
		SpringApplication.run(CartServiceApplication.class, args);
	}

	private static void setSpringProperties(Dotenv dotenv) {
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST", ""));
		System.setProperty("POSTGRES_USERNAME", dotenv.get("POSTGRES_USERNAME", ""));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD", ""));
		
		String productServiceUrl = dotenv.get("PRODUCT_SERVICE_URL");
		if (productServiceUrl != null && !productServiceUrl.isEmpty()) {
			System.setProperty("PRODUCT_SERVICE_URL", productServiceUrl);
		}
		
		String orderServiceUrl = dotenv.get("ORDER_SERVICE_URL");
		if (orderServiceUrl != null && !orderServiceUrl.isEmpty()) {
			System.setProperty("ORDER_SERVICE_URL", orderServiceUrl);
		}
		
		String paymentServiceUrl = dotenv.get("PAYMENT_SERVICE_URL");
		if (paymentServiceUrl != null && !paymentServiceUrl.isEmpty()) {
			System.setProperty("PAYMENT_SERVICE_URL", paymentServiceUrl);
		}
		
		String promotionServiceUrl = dotenv.get("PROMOTION_SERVICE_URL");
		if (promotionServiceUrl != null && !promotionServiceUrl.isEmpty()) {
			System.setProperty("PROMOTION_SERVICE_URL", promotionServiceUrl);
		}
		
		String shopServiceUrl = dotenv.get("SHOP_SERVICE_URL");
		if (shopServiceUrl != null && !shopServiceUrl.isEmpty()) {
			System.setProperty("SHOP_SERVICE_URL", shopServiceUrl);
		}
		
		System.setProperty("AUTH0_DOMAIN", dotenv.get("AUTH0_DOMAIN", ""));
	}
}
