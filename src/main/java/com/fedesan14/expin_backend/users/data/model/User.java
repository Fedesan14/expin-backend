package com.fedesan14.expin_backend.users.data.model;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "AuthUser")
@Table(name = "auth_users")
public class User implements UserDetails {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, unique = true, length = 30)
	private String username;

	@Column(nullable = false)
	private String password;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "profile_id", nullable = false, unique = true)
	private Profile profile;

    @Column
    private String autologinHash;

	public User(String username, String password, Profile profile) {
		this.id = UUID.randomUUID();
		this.username = username;
		this.password = password;
		this.profile = profile;
	}

	public UUID getProfileId() {
		return profile.getId();
	}

	public User withProfile(Profile profile) {
		this.profile = profile;
		return this;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
