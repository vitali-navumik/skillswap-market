WITH replacement_mentor AS (
    SELECT id
    FROM app_users
    WHERE email = 'mentor1@test.com'
),
admin_owned_offers AS (
    SELECT offer.id
    FROM skill_offers offer
    JOIN app_users owner ON owner.id = offer.mentor_id
    WHERE NOT EXISTS (
        SELECT 1
        FROM user_roles role
        WHERE role.user_id = owner.id
          AND role.role = 'MENTOR'
    )
)
UPDATE bookings booking
SET mentor_id = (SELECT id FROM replacement_mentor)
WHERE booking.offer_id IN (SELECT id FROM admin_owned_offers)
  AND EXISTS (SELECT 1 FROM replacement_mentor);

WITH replacement_mentor AS (
    SELECT id
    FROM app_users
    WHERE email = 'mentor1@test.com'
),
admin_owned_offers AS (
    SELECT offer.id
    FROM skill_offers offer
    JOIN app_users owner ON owner.id = offer.mentor_id
    WHERE NOT EXISTS (
        SELECT 1
        FROM user_roles role
        WHERE role.user_id = owner.id
          AND role.role = 'MENTOR'
    )
)
UPDATE reviews review
SET target_user_id = (SELECT id FROM replacement_mentor)
WHERE review.offer_id IN (SELECT id FROM admin_owned_offers)
  AND review.target_user_id <> (SELECT id FROM replacement_mentor)
  AND EXISTS (SELECT 1 FROM replacement_mentor);

WITH replacement_mentor AS (
    SELECT id
    FROM app_users
    WHERE email = 'mentor1@test.com'
)
UPDATE skill_offers offer
SET mentor_id = (SELECT id FROM replacement_mentor)
WHERE NOT EXISTS (
    SELECT 1
    FROM user_roles role
    WHERE role.user_id = offer.mentor_id
      AND role.role = 'MENTOR'
)
  AND EXISTS (SELECT 1 FROM replacement_mentor);
