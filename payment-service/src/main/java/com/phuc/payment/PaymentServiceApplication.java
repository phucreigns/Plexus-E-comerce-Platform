package com.phuc.payment;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.filename(".env")
				.ignoreIfMissing()
				.load();
		setSpringProperties(dotenv);
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

	private static void setSpringProperties(Dotenv dotenv) {
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST", "localhost"));
		System.setProperty("POSTGRES_PORT", dotenv.get("POSTGRES_PORT", "5432"));
		System.setProperty("POSTGRES_USERNAME", dotenv.get("POSTGRES_USERNAME", ""));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD", ""));
		String paymentDb = dotenv.get("PAYMENTSERVICE_SERVICE_DB", "");
		if (paymentDb.isBlank()) {
			paymentDb = dotenv.get("PAYMENT_SERVICE_DB", "PaymentService");
		}
		System.setProperty("PAYMENT_SERVICE_DB", paymentDb);
		
		System.setProperty("AUTH0_DOMAIN", dotenv.get("AUTH0_DOMAIN", ""));

		System.setProperty("STRIPE_API_KEY", dotenv.get("STRIPE_API_KEY", ""));
		String webhookSecret = dotenv.get("STRIPE_WEBHOOK_SECRET", "");
		if (webhookSecret.isBlank()) {
			webhookSecret = dotenv.get("STRIPE_ENDPOINT_SECRET", "");
		}
		System.setProperty("STRIPE_WEBHOOK_SECRET", webhookSecret);
	
	}
}
