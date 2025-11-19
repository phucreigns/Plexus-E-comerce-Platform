package com.phuc.file;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.ignoreIfMissing()
				.load();
		setSpringProperties(dotenv);
		SpringApplication.run(FileServiceApplication.class, args);
	}

	private static void setSpringProperties(Dotenv dotenv) {
		System.setProperty("MONGO_HOST", dotenv.get("MONGO_HOST", ""));
		System.setProperty("MONGO_USERNAME", dotenv.get("MONGO_USERNAME", ""));
		System.setProperty("MONGO_PASSWORD", dotenv.get("MONGO_PASSWORD", ""));
		System.setProperty("MONGO_PORT", dotenv.get("MONGO_PORT", ""));
		System.setProperty("MONGO_DB", dotenv.get("MONGO_DB", ""));

		System.setProperty("AWS_S3_BUCKET", dotenv.get("AWS_S3_BUCKET", ""));
		System.setProperty("AWS_S3_REGION", dotenv.get("AWS_S3_REGION", ""));
		System.setProperty("AWS_S3_ACCESS_KEY", dotenv.get("AWS_S3_ACCESS_KEY", ""));
		System.setProperty("AWS_S3_SECRET_KEY", dotenv.get("AWS_S3_SECRET_KEY", ""));
		
        System.setProperty("AUTH0_DOMAIN", dotenv.get("AUTH0_DOMAIN", ""));
	}
}
