package com.fedesan14.expin_backend.events.service.interfaces;

public interface EventShareLinkGenerator {

	String generate();

	String fromToken(String token);
}
