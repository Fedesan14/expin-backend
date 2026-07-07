package com.fedesan14.expin_backend.auth.service;

import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;

public interface UserService {

	User findById(UUID userId);
}
