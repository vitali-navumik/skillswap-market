ALTER TABLE bookings
    DROP CONSTRAINT IF EXISTS uk_bookings_slot_id;

CREATE UNIQUE INDEX IF NOT EXISTS uk_bookings_reserved_slot_id
    ON bookings (slot_id)
    WHERE status = 'RESERVED';
