ALTER TABLE skill_offers
    ADD COLUMN idempotency_key VARCHAR(200);

ALTER TABLE skill_offers
    ADD CONSTRAINT uk_skill_offers_mentor_idempotency_key UNIQUE (mentor_id, idempotency_key);
