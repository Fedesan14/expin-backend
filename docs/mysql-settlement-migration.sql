USE expin;

CREATE TABLE event_participant_balance (
  id BINARY(16) NOT NULL,
  participant_id BINARY(16) NULL,
  display_name VARCHAR(255) NULL,
  paid_amount DECIMAL(38, 2) NULL,
  owed_amount DECIMAL(38, 2) NULL,
  balance DECIMAL(38, 2) NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_transfer (
  id BINARY(16) NOT NULL,
  from_participant_id BINARY(16) NULL,
  to_participant_id BINARY(16) NULL,
  from_display_name VARCHAR(255) NULL,
  to_display_name VARCHAR(255) NULL,
  amount DECIMAL(38, 2) NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_settlement (
  id BINARY(16) NOT NULL,
  strategy ENUM('OWNER_CENTRIC') NULL,
  total_amount DECIMAL(38, 2) NULL,
  participant_count INT NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_settlement_balances (
  event_settlement_id BINARY(16) NOT NULL,
  balances_id BINARY(16) NOT NULL,
  PRIMARY KEY (event_settlement_id, balances_id),
  CONSTRAINT uk_event_settlement_balances_balance UNIQUE (balances_id),
  CONSTRAINT fk_event_settlement_balances_settlement
    FOREIGN KEY (event_settlement_id) REFERENCES event_settlement (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_event_settlement_balances_balance
    FOREIGN KEY (balances_id) REFERENCES event_participant_balance (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_settlement_transfers (
  event_settlement_id BINARY(16) NOT NULL,
  transfers_id BINARY(16) NOT NULL,
  PRIMARY KEY (event_settlement_id, transfers_id),
  CONSTRAINT uk_event_settlement_transfers_transfer UNIQUE (transfers_id),
  CONSTRAINT fk_event_settlement_transfers_settlement
    FOREIGN KEY (event_settlement_id) REFERENCES event_settlement (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_event_settlement_transfers_transfer
    FOREIGN KEY (transfers_id) REFERENCES event_transfer (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE events
  ADD COLUMN settlement_id BINARY(16) NULL,
  ADD COLUMN status TINYINT NOT NULL DEFAULT 0,
  ADD CONSTRAINT uk_events_settlement_id UNIQUE (settlement_id),
  ADD CONSTRAINT fk_events_settlement
    FOREIGN KEY (settlement_id) REFERENCES event_settlement (id);
