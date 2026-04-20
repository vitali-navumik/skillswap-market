DELETE FROM wallet_transactions
WHERE wallet_id IN (
    SELECT id
    FROM wallets
    WHERE user_id IN (
        SELECT id
        FROM app_users
        WHERE email = 'student.phase1.1774032472@test.com'
    )
);

DELETE FROM wallets
WHERE user_id IN (
    SELECT id
    FROM app_users
    WHERE email = 'student.phase1.1774032472@test.com'
);

DELETE FROM user_roles
WHERE user_id IN (
    SELECT id
    FROM app_users
    WHERE email = 'student.phase1.1774032472@test.com'
);

DELETE FROM app_users
WHERE email = 'student.phase1.1774032472@test.com';
