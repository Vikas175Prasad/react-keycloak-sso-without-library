package com.vikas.keycloak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
public class KeycloakAppbApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeycloakAppbApplication.class, args);
	}

}
