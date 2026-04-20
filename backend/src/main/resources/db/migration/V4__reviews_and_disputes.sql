CREATE TABLE reviews
(
    id             BIGSERIAL PRIMARY KEY,
    booking_id     BIGINT                   NOT NULL,
    author_id      BIGINT                   NOT NULL,
    target_user_id BIGINT                   NOT NULL,
    rating         INTEGER                  NOT NULL,
    comment        TEXT                     NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_author FOREIGN KEY (author_id) REFERENCES app_users (id),
    CONSTRAINT fk_reviews_target FOREIGN KEY (target_user_id) REFERENCES app_users (id),
    CONSTRAINT uk_reviews_booking_author UNIQUE (booking_id, author_id)
);

CREATE TABLE disputes
(
    id          BIGSERIAL PRIMARY KEY,
    booking_id   BIGINT                   NOT NULL,
    created_by   BIGINT                   NOT NULL,
    reason       VARCHAR(255)             NOT NULL,
    description  TEXT                     NOT NULL,
    status       VARCHAR(32)              NOT NULL,
    resolution   TEXT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_disputes_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_disputes_created_by FOREIGN KEY (created_by) REFERENCES app_users (id)
);

CREATE INDEX idx_reviews_booking_id ON reviews (booking_id);
CREATE INDEX idx_disputes_booking_id ON disputes (booking_id);
CREATE INDEX idx_disputes_status ON disputes (status);
