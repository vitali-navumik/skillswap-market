ALTER TABLE reviews
    ADD COLUMN offer_id BIGINT,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN created_in_admin_scope BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE reviews r
SET offer_id = b.offer_id,
    updated_at = COALESCE(r.created_at, CURRENT_TIMESTAMP)
FROM bookings b
WHERE r.booking_id = b.id;

ALTER TABLE reviews
    ALTER COLUMN offer_id SET NOT NULL,
    ALTER COLUMN booking_id DROP NOT NULL;

ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_offer FOREIGN KEY (offer_id) REFERENCES skill_offers (id) ON DELETE CASCADE;

CREATE INDEX idx_reviews_offer_id ON reviews (offer_id);
