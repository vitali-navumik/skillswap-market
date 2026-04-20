CREATE TEMP TABLE tmp_wallet_cancelled_payout_bookings AS
SELECT DISTINCT booking_id
FROM wallet_transactions
WHERE booking_id IS NOT NULL
  AND type = 'PAYOUT';

DELETE
FROM wallet_transactions
WHERE booking_id IS NOT NULL;

INSERT INTO wallet_transactions (wallet_id, booking_id, type, amount, status, created_at)
SELECT wallets.id,
       bookings.id,
       'CHARGE',
       bookings.price_credits,
       'COMPLETED',
       bookings.created_at
FROM bookings
         JOIN wallets ON wallets.user_id = bookings.student_id;

INSERT INTO wallet_transactions (wallet_id, booking_id, type, amount, status, created_at)
SELECT wallets.id,
       bookings.id,
       'REFUND',
       bookings.price_credits,
       'COMPLETED',
       bookings.updated_at
FROM bookings
         JOIN wallets ON wallets.user_id = bookings.student_id
WHERE bookings.status = 'CANCELLED';

INSERT INTO wallet_transactions (wallet_id, booking_id, type, amount, status, created_at)
SELECT wallets.id,
       bookings.id,
       'PAYOUT',
       bookings.price_credits,
       'COMPLETED',
       CASE
           WHEN bookings.status = 'COMPLETED' THEN bookings.updated_at
           ELSE bookings.created_at + INTERVAL '1 second'
           END
FROM bookings
         JOIN wallets ON wallets.user_id = bookings.mentor_id
WHERE bookings.status = 'COMPLETED'
   OR (bookings.status = 'CANCELLED' AND EXISTS(
        SELECT 1
        FROM tmp_wallet_cancelled_payout_bookings
        WHERE tmp_wallet_cancelled_payout_bookings.booking_id = bookings.id
    ));

INSERT INTO wallet_transactions (wallet_id, booking_id, type, amount, status, created_at)
SELECT wallets.id,
       bookings.id,
       'ADJUSTMENT',
       -bookings.price_credits,
       'COMPLETED',
       bookings.updated_at + INTERVAL '1 second'
FROM bookings
         JOIN wallets ON wallets.user_id = bookings.mentor_id
WHERE bookings.status = 'CANCELLED'
  AND EXISTS(
        SELECT 1
        FROM tmp_wallet_cancelled_payout_bookings
        WHERE tmp_wallet_cancelled_payout_bookings.booking_id = bookings.id
    );

UPDATE wallets
SET reserved_balance = 0;

UPDATE wallets
SET balance = COALESCE(ledger.balance, 0)
FROM (
         SELECT wallet_id,
                SUM(
                        CASE type
                            WHEN 'TOP_UP' THEN amount
                            WHEN 'REFUND' THEN amount
                            WHEN 'PAYOUT' THEN amount
                            WHEN 'ADJUSTMENT' THEN amount
                            WHEN 'CHARGE' THEN -amount
                            ELSE 0
                            END
                ) AS balance
         FROM wallet_transactions
         GROUP BY wallet_id
     ) AS ledger
WHERE wallets.id = ledger.wallet_id;

UPDATE wallets
SET balance = 0
WHERE id NOT IN (
    SELECT DISTINCT wallet_id
    FROM wallet_transactions
);

DROP TABLE tmp_wallet_cancelled_payout_bookings;
