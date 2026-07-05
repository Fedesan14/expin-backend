package com.fedesan14.expin_backend.common.controller.implementations;

import com.fedesan14.expin_backend.common.controller.interfaces.PingController;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingControllerImpl implements PingController {

	@Override
	public String ping() {
		return "pong";
	}
}
