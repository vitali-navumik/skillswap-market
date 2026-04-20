UPDATE bookings
SET status = 'RESERVED'
WHERE status = 'IN_PROGRESS';

UPDATE bookings
SET status = 'CANCELLED',
    no_show_side = NULL
WHERE status = 'NO_SHOW';
