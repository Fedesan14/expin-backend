package com.fedesan14.expin_backend.users.services.implementations;

import java.util.Optional;
import java.util.UUID;

import com.fedesan14.expin_backend.users.data.repository.ProfileRepository;
import com.fedesan14.expin_backend.users.services.interfaces.UserService;
import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.users.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.fedesan14.expin_backend.common.utils.EmailUtils.normalizeEmail;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

	@Override
	public User findById(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

    @Override
    public boolean validateUsernameAlreadyExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean validateEmailAlreadyExists(String email) {
        return profileRepository.existsByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByAutologinHashAndUsername(String autologinHash, String username) {
        return userRepository.findByAutologinHashAndUsername(autologinHash, username)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    @Override
    public User findByUsernameOrProfileEmail(String identifier, String email) {
        return userRepository.findByUsernameOrProfileEmail(identifier, normalizeEmail(identifier))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }
}
