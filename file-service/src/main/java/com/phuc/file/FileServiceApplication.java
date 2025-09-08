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
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST", ""));
		System.setProperty("POSTGRES_USERNAME", dotenv.get("POSTGRES_USERNAME", ""));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD", ""));
	}
}
