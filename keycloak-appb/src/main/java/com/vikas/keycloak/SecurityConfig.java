package com.vikas.keycloak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		return http.csrf().disable()
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/login","/api/callback","/api/logout")
						.permitAll().anyRequest().authenticated()).httpBasic().disable().formLogin().disable().build();
	}
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("http://localhost:4000","http://localhost:8080").allowedMethods("GET", "POST", "PUT", "DELETE")
				.allowCredentials(true);
			}
		};
	}
}
