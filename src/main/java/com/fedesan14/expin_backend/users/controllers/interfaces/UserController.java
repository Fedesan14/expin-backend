package com.fedesan14.expin_backend.users.controllers.interfaces;

import com.fedesan14.expin_backend.auth.controller.responses.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/users")
public interface UserController {
    @GetMapping
    List<UserResponse> getUsers(@RequestParam(required = false) String identifier);
}
