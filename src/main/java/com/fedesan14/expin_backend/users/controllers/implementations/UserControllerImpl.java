package com.fedesan14.expin_backend.users.controllers.implementations;

import com.fedesan14.expin_backend.auth.controller.responses.UserResponse;
import com.fedesan14.expin_backend.users.controllers.interfaces.UserController;
import com.fedesan14.expin_backend.users.services.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    public List<UserResponse> getUsers(String identifier) {
        return userService.getUsers(identifier);
    }
}
