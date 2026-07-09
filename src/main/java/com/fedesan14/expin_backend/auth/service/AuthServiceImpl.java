package com.fedesan14.expin_backend.auth.service;

import java.util.Locale;

import com.fedesan14.expin_backend.auth.controller.requests.SignUpRequest;
import com.fedesan14.expin_backend.auth.controller.responses.AuthTokensResponse;
import com.fedesan14.expin_backend.users.data.model.Profile;
import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.users.data.repository.ProfileRepository;
import com.fedesan14.expin_backend.users.data.repository.UserRepository;
import com.fedesan14.expin_backend.auth.security.basic.BasicCredentials;
import com.fedesan14.expin_backend.auth.security.jwt.JwtService;
import com.fedesan14.expin_backend.auth.security.jwt.JwtTokenPair;
import com.fedesan14.expin_backend.users.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static com.fedesan14.expin_backend.common.utils.EmailUtils.normalizeEmail;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Override
	@Transactional
	public User signup(SignUpRequest request) {
		String username = request.username().trim();
		String email = normalizeEmail(request.email());

        validateUsernameAndEmail(username, email);

        try {
			return userService.saveUser(
                    new User(username,
                            passwordEncoder.encode(request.password()),
                            new Profile(email)
                    )
            );
		} catch (DataIntegrityViolationException exception) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists", exception);
		}
	}

    @Override
	public AuthTokensResponse login(BasicCredentials credentials, String autologinHash) {
        User user = getUserAndValidateCredentials(credentials);
        saveAutologin(autologinHash, user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthTokensResponse autologin(String autologinHash, String username) {
        User user = getUserByAutologinHashAndUsername(autologinHash, username);
        return buildAuthResponse(user);
    }

    private User getUserByAutologinHashAndUsername(String autologinHash, String username) {
        return userService.findByAutologinHashAndUsername(autologinHash, username);
    }

    private AuthTokensResponse buildAuthResponse(User user) {
        JwtTokenPair tokenPair = jwtService.createTokenPair(user);
        return new AuthTokensResponse(
                tokenPair.sessionToken(),
                tokenPair.sessionTokenExpiresAt(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenExpiresAt()
        );
    }

    private void saveAutologin(String autologinHash, User user) {
        if (autologinHash != null) {
            user.setAutologinHash(autologinHash);
            userService.saveUser(user);
        }
    }

    private User getUserAndValidateCredentials(BasicCredentials credentials) {
        User user = findByUsernameOrEmail(credentials.identifier().trim());
        if (!passwordEncoder.matches(credentials.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return user;
    }

    private User findByUsernameOrEmail(String identifier) {
		return userService.findByUsernameOrProfileEmail(identifier, normalizeEmail(identifier));
	}

    private void validateUsernameAndEmail(String username, String email) {
        if (userService.validateUsernameAlreadyExists(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userService.validateEmailAlreadyExists(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
    }
}
