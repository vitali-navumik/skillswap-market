CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE app_users
    ADD COLUMN public_id UUID;

UPDATE app_users
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE app_users
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE app_users
    ADD CONSTRAINT uk_app_users_public_id UNIQUE (public_id);

ALTER TABLE skill_offers
    ADD COLUMN public_id UUID;

UPDATE skill_offers
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE skill_offers
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE skill_offers
    ADD CONSTRAINT uk_skill_offers_public_id UNIQUE (public_id);

ALTER TABLE bookings
    ADD COLUMN public_id UUID;

UPDATE bookings
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE bookings
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE bookings
    ADD CONSTRAINT uk_bookings_public_id UNIQUE (public_id);

ALTER TABLE reviews
    ADD COLUMN public_id UUID;

UPDATE reviews
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE reviews
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE reviews
    ADD CONSTRAINT uk_reviews_public_id UNIQUE (public_id);

ALTER TABLE disputes
    ADD COLUMN public_id UUID;

UPDATE disputes
SET public_id = gen_random_uuid()
WHERE public_id IS NULL;

ALTER TABLE disputes
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE disputes
    ADD CONSTRAINT uk_disputes_public_id UNIQUE (public_id);
