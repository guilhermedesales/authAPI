package com.api.auth;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthApplication {

	public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load(); // carrega .env
        setIfPresent("JWT_SECRET", dotenv.get("JWT_SECRET"));
        setIfPresent("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));
        setIfPresent("JWT_REFRESH_EXPIRATION", dotenv.get("JWT_REFRESH_EXPIRATION"));

        setIfPresent("VERIFICATION_CODE_EXPIRATION", dotenv.get("VERIFICATION_CODE_EXPIRATION"));
        setIfPresent("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
        setIfPresent("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
        setIfPresent("CORS_ORIGIN_1", dotenv.get("CORS_ORIGIN_1"));
        setIfPresent("CORS_ORIGIN_2", dotenv.get("CORS_ORIGIN_2"));
        setIfPresent("CORS_ORIGIN_3", dotenv.get("CORS_ORIGIN_3"));

		SpringApplication.run(AuthApplication.class, args);
	}

    private static void setIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
    }

}
