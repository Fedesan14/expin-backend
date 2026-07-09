package com.fedesan14.expin_backend.users.data.repository;

import java.util.Optional;
import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByUsername(String username);

	@Query("""
		SELECT authUser
		FROM AuthUser authUser
		JOIN authUser.profile profile
		WHERE authUser.username = :username OR profile.email = :email
		""")
	Optional<User> findByUsernameOrProfileEmail(@Param("username") String username, @Param("email") String email);

    @Query("""
            SELECT authUser
            FROM AuthUser authUser
            JOIN authUser.profile profile
            WHERE (authUser.username = :username OR profile.email = :username) AND (authUser.autologinHash = :autologinHash)
        """)
    Optional<User> findByAutologinHashAndUsername(String autologinHash, String username);
}
