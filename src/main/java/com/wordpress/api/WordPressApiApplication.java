package com.wordpress.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class WordPressApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordPressApiApplication.class, args);
	}

	@Configuration
	public class CorsConfig implements WebMvcConfigurer {
		@Bean
		public WebMvcConfigurer corsConfigurer() {
			return new WebMvcConfigurer() {
				@Override
				public void addCorsMappings(CorsRegistry registry) {
					registry.addMapping("/WordPress/api/*").allowedOrigins("*");
				}
			};
		}
	}
}