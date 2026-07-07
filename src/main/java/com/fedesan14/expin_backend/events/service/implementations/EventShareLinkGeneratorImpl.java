package com.fedesan14.expin_backend.events.service.implementations;

import java.security.SecureRandom;

import com.fedesan14.expin_backend.events.data.repository.EventRepository;
import com.fedesan14.expin_backend.events.service.interfaces.EventShareLinkGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventShareLinkGeneratorImpl implements EventShareLinkGenerator {

	private static final String SHARE_LINK_PREFIX = "/events/invite/";
	private static final String SHARE_LINK_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int SHARE_LINK_TOKEN_LENGTH = 12;

	private final EventRepository eventRepository;
	private final SecureRandom secureRandom;

	@Override
	public String generate() {
		String shareLink;
		do {
			shareLink = fromToken(randomToken());
		} while (eventRepository.existsByShareLink(shareLink));
		return shareLink;
	}

	@Override
	public String fromToken(String token) {
		return SHARE_LINK_PREFIX + token;
	}

	private String randomToken() {
		StringBuilder token = new StringBuilder(SHARE_LINK_TOKEN_LENGTH);
		for (int index = 0; index < SHARE_LINK_TOKEN_LENGTH; index++) {
			token.append(SHARE_LINK_ALPHABET.charAt(secureRandom.nextInt(SHARE_LINK_ALPHABET.length())));
		}
		return token.toString();
	}
}
