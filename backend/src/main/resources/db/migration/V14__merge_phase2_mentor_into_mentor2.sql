WITH phase2_user AS (
    SELECT id
    FROM app_users
    WHERE email LIKE 'mentor.phase2.%@test.com'
),
mentor_two AS (
    SELECT id
    FROM app_users
    WHERE email = 'mentor2@test.com'
)
UPDATE skill_offers offer
SET mentor_id = (SELECT id FROM mentor_two)
WHERE offer.mentor_id IN (SELECT id FROM phase2_user)
  AND EXISTS (SELECT 1 FROM mentor_two);

WITH phase2_user AS (
    SELECT id
    FROM app_users
    WHERE email LIKE 'mentor.phase2.%@test.com'
),
mentor_two AS (
    SELECT id
    FROM app_users
    WHERE email = 'mentor2@test.com'
)
UPDATE bookings booking
SET mentor_id = (SELECT id FROM mentor_two)
WHERE booking.mentor_id IN (SELECT id FROM phase2_user)
  AND EXISTS (SELECT 1 FROM mentor_two);

WITH phase2_user AS (
    SELECT id
    FROM app_users
    WHERE email LIKE 'mentor.phase2.%@test.com'
),
mentor_two AS (
    SELECT id
    FROM app_users
    WHERE email = 'mentor2@test.com'
)
UPDATE reviews review
SET author_id = (SELECT id FROM mentor_two)
WHERE review.author_id IN (SELECT id FROM phase2_user)
  AND EXISTS (SELECT 1 FROM mentor_two);

WITH phase2_user AS (
    SELECT id
    FROM app_users
    WHERE email LIKE 'mentor.phase2.%@test.com'
),
mentor_two AS (
    SELECT id
    FROM app_users
    WHERE email = 'mentor2@test.com'
)
UPDATE reviews review
SET target_user_id = (SELECT id FROM mentor_two)
WHERE review.target_user_id IN (SELECT id FROM phase2_user)
  AND EXISTS (SELECT 1 FROM mentor_two);

WITH phase2_user AS (
    SELECT id
    FROM app_users
    WHERE email LIKE 'mentor.phase2.%@test.com'
),
mentor_two AS (
    SELECT id
    FROM app_users
    WHERE email = 'mentor2@test.com'
)
UPDATE disputes dispute
SET created_by = (SELECT id FROM mentor_two)
WHERE dispute.created_by IN (SELECT id FROM phase2_user)
  AND EXISTS (SELECT 1 FROM mentor_two);

DELETE FROM wallet_transactions
WHERE wallet_id IN (
    SELECT id
    FROM wallets
    WHERE user_id IN (
        SELECT id
        FROM app_users
        WHERE email LIKE 'mentor.phase2.%@test.com'
    )
);

DELETE FROM wallets
WHERE user_id IN (
    SELECT id
    FROM app_users
    WHERE email LIKE 'mentor.phase2.%@test.com'
);

DELETE FROM user_roles
WHERE user_id IN (
    SELECT id
    FROM app_users
    WHERE email LIKE 'mentor.phase2.%@test.com'
);

DELETE FROM app_users
WHERE email LIKE 'mentor.phase2.%@test.com';
