UPDATE app_users
SET first_name = 'Mentor',
    last_name = 'Two',
    display_name = 'Mentor Two'
WHERE email LIKE 'mentor.phase2.%@test.com';
