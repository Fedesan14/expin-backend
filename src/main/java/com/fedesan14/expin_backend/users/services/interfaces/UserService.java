package com.fedesan14.expin_backend.users.services.interfaces;

import java.util.Optional;
import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;

public interface UserService {

	User findById(UUID userId);

    boolean validateUsernameAlreadyExists(String username);

    boolean validateEmailAlreadyExists(String email);

    User saveUser(User user);

    User findByAutologinHashAndUsername(String autologinHash, String username);

    User findByUsernameOrProfileEmail(String identifier, String email);
}
