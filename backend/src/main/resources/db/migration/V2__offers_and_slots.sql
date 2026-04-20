CREATE TABLE skill_offers
(
    id                        BIGSERIAL PRIMARY KEY,
    mentor_id                 BIGINT                      NOT NULL,
    title                     VARCHAR(200)                NOT NULL,
    description               TEXT                        NOT NULL,
    category                  VARCHAR(100)                NOT NULL,
    duration_minutes          INTEGER                     NOT NULL,
    price_credits             INTEGER                     NOT NULL,
    cancellation_policy_hours INTEGER                     NOT NULL,
    status                    VARCHAR(32)                 NOT NULL,
    created_at                TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_skill_offers_mentor FOREIGN KEY (mentor_id) REFERENCES app_users (id)
);

CREATE TABLE availability_slots
(
    id         BIGSERIAL PRIMARY KEY,
    offer_id    BIGINT                   NOT NULL,
    start_time  TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time    TIMESTAMP WITH TIME ZONE NOT NULL,
    status      VARCHAR(32)              NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_availability_slots_offer FOREIGN KEY (offer_id) REFERENCES skill_offers (id) ON DELETE CASCADE
);

CREATE INDEX idx_skill_offers_mentor_id ON skill_offers (mentor_id);
CREATE INDEX idx_skill_offers_status ON skill_offers (status);
CREATE INDEX idx_skill_offers_category ON skill_offers (category);
CREATE INDEX idx_availability_slots_offer_id ON availability_slots (offer_id);
CREATE INDEX idx_availability_slots_time ON availability_slots (start_time, end_time);
