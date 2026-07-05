package com.fedesan14.expin_backend.common.controller.interfaces;

import org.springframework.web.bind.annotation.GetMapping;

public interface PingController {

	@GetMapping("/ping")
	String ping();
}
