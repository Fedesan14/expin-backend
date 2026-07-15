CREATE DATABASE IF NOT EXISTS expin
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE expin;

CREATE TABLE IF NOT EXISTS profiles (
  id BINARY(16) NOT NULL,
  email VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_profiles_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS auth_users (
  id BINARY(16) NOT NULL,
  username VARCHAR(30) NOT NULL,
  password VARCHAR(255) NOT NULL,
  profile_id BINARY(16) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_auth_users_username UNIQUE (username),
  CONSTRAINT uk_auth_users_profile_id UNIQUE (profile_id),
  CONSTRAINT fk_auth_users_profile
    FOREIGN KEY (profile_id) REFERENCES profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS events (
  id BINARY(16) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  share_link VARCHAR(255) NOT NULL,
  owner_id BINARY(16) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_events_share_link UNIQUE (share_link),
  CONSTRAINT fk_events_owner
    FOREIGN KEY (owner_id) REFERENCES auth_users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS event_participants (
  id BINARY(16) NOT NULL,
  event_id BINARY(16) NOT NULL,
  user_id BINARY(16) NULL,
  guest_name VARCHAR(255) NULL,
  type ENUM('USER', 'GUEST') NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_event_participants_event
    FOREIGN KEY (event_id) REFERENCES events (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_event_participants_user
    FOREIGN KEY (user_id) REFERENCES auth_users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS event_expenses (
  id BINARY(16) NOT NULL,
  event_id BINARY(16) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  amount DECIMAL(19, 2) NOT NULL,
  paid_by_participant_id BINARY(16) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_event_expenses_event
    FOREIGN KEY (event_id) REFERENCES events (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_event_expenses_paid_by_participant
    FOREIGN KEY (paid_by_participant_id) REFERENCES event_participants (id),
  CONSTRAINT ck_event_expenses_amount_positive CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS event_expense_debtors (
  expense_id BINARY(16) NOT NULL,
  participant_id BINARY(16) NOT NULL,
  PRIMARY KEY (expense_id, participant_id),
  CONSTRAINT fk_event_expense_debtors_expense
    FOREIGN KEY (expense_id) REFERENCES event_expenses (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_event_expense_debtors_participant
    FOREIGN KEY (participant_id) REFERENCES event_participants (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_events_owner_id
  ON events (owner_id);

CREATE INDEX idx_event_participants_event_id
  ON event_participants (event_id);

CREATE INDEX idx_event_participants_user_id
  ON event_participants (user_id);

CREATE INDEX idx_event_participants_event_user
  ON event_participants (event_id, user_id);

CREATE INDEX idx_event_expenses_event_id
  ON event_expenses (event_id);

CREATE INDEX idx_event_expenses_paid_by_participant_id
  ON event_expenses (paid_by_participant_id);

CREATE INDEX idx_event_expense_debtors_participant_id
  ON event_expense_debtors (participant_id);

ALTER TABLE auth_users ADD COLUMN autologin_hash varchar(255) default null;