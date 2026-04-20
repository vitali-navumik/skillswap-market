CREATE TABLE wallets
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT                   NOT NULL,
    balance          INTEGER                  NOT NULL DEFAULT 0,
    reserved_balance INTEGER                  NOT NULL DEFAULT 0,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_wallets_user_id UNIQUE (user_id),
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
);

CREATE TABLE bookings
(
    id                   BIGSERIAL PRIMARY KEY,
    slot_id              BIGINT                   NOT NULL,
    offer_id             BIGINT                   NOT NULL,
    student_id           BIGINT                   NOT NULL,
    mentor_id            BIGINT                   NOT NULL,
    status               VARCHAR(32)              NOT NULL,
    price_credits        INTEGER                  NOT NULL,
    reserved_amount      INTEGER                  NOT NULL,
    cancelled_by_user_id BIGINT,
    no_show_side         VARCHAR(32),
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_bookings_slot_id UNIQUE (slot_id),
    CONSTRAINT fk_bookings_slot FOREIGN KEY (slot_id) REFERENCES availability_slots (id),
    CONSTRAINT fk_bookings_offer FOREIGN KEY (offer_id) REFERENCES skill_offers (id),
    CONSTRAINT fk_bookings_student FOREIGN KEY (student_id) REFERENCES app_users (id),
    CONSTRAINT fk_bookings_mentor FOREIGN KEY (mentor_id) REFERENCES app_users (id),
    CONSTRAINT fk_bookings_cancelled_by FOREIGN KEY (cancelled_by_user_id) REFERENCES app_users (id)
);

CREATE TABLE wallet_transactions
(
    id         BIGSERIAL PRIMARY KEY,
    wallet_id   BIGINT                   NOT NULL,
    booking_id  BIGINT,
    type        VARCHAR(32)              NOT NULL,
    amount      INTEGER                  NOT NULL,
    status      VARCHAR(32)              NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_transactions_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE SET NULL
);

CREATE INDEX idx_wallet_transactions_wallet_id ON wallet_transactions (wallet_id);
CREATE INDEX idx_wallet_transactions_booking_id ON wallet_transactions (booking_id);
CREATE INDEX idx_bookings_student_id ON bookings (student_id);
CREATE INDEX idx_bookings_mentor_id ON bookings (mentor_id);
CREATE INDEX idx_bookings_status ON bookings (status);

INSERT INTO wallets (user_id, balance, reserved_balance, updated_at)
SELECT id, 0, 0, CURRENT_TIMESTAMP
FROM app_users
ON CONFLICT (user_id) DO NOTHING;
