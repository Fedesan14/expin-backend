package com.fedesan14.expin_backend.auth.data.repository;

import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

	boolean existsByEmail(String email);
}
