package com.phuc.auth;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AuthServiceApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.ignoreIfMissing()
				.load();
		setSpringProperties(dotenv);
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	private static void setSpringProperties(Dotenv dotenv) {
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST", ""));
		System.setProperty("POSTGRES_USERNAME", dotenv.get("POSTGRES_USERNAME", ""));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD", ""));


		String auth0IssuerUri = dotenv.get("AUTH0_ISSUER_URI", "");
		if (auth0IssuerUri.startsWith("http://")) {
			auth0IssuerUri = "https://" + auth0IssuerUri.substring(7);
		} else if (!auth0IssuerUri.startsWith("https://")) {
			auth0IssuerUri = "https://" + auth0IssuerUri;
		}
		System.setProperty("AUTH0_ISSUER_URI", auth0IssuerUri);

		System.setProperty("AUTH0_CLIENT_ID", dotenv.get("AUTH0_CLIENT_ID", ""));
		System.setProperty("AUTH0_CLIENT_SECRET", dotenv.get("AUTH0_CLIENT_SECRET", ""));
		System.setProperty("AUTH0_REDIRECT_URI", dotenv.get("AUTH0_REDIRECT_URI", ""));
		System.setProperty("AUTH0_AUDIENCE", dotenv.get("AUTH0_AUDIENCE", ""));

		String auth0Domain = dotenv.get("AUTH0_DOMAIN", "");
		if (auth0Domain.startsWith("https://")) {
			auth0Domain = auth0Domain.substring(8);
		} else if (auth0Domain.startsWith("http://")) {
			auth0Domain = auth0Domain.substring(7);
		}
		System.setProperty("AUTH0_DOMAIN", auth0Domain);
	}
}
