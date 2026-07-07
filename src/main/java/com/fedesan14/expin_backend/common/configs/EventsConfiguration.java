package com.fedesan14.expin_backend.common.configs;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventsConfiguration {

	@Bean
	public SecureRandom secureRandom() {
		return new SecureRandom();
	}
}
