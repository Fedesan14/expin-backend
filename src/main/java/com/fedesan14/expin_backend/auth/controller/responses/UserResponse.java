package com.fedesan14.expin_backend.auth.controller.responses;

import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;

public record UserResponse(UUID id, String username, UUID profileId, String email) {

	public static UserResponse from(User user) {
		return new UserResponse(
			user.getId(),
			user.getUsername(),
			user.getProfileId(),
			user.getProfile().getEmail()
		);
	}
}
