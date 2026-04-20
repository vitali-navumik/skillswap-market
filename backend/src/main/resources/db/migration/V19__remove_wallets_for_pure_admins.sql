DELETE FROM wallet_transactions
WHERE wallet_id IN (
    SELECT wallets.id
    FROM wallets
             JOIN app_users ON app_users.id = wallets.user_id
    WHERE EXISTS (
        SELECT 1
        FROM user_roles admin_role
        WHERE admin_role.user_id = app_users.id
          AND admin_role.role = 'ADMIN'
    )
      AND NOT EXISTS (
        SELECT 1
        FROM user_roles product_role
        WHERE product_role.user_id = app_users.id
          AND product_role.role IN ('STUDENT', 'MENTOR')
    )
);

DELETE FROM wallets
WHERE user_id IN (
    SELECT app_users.id
    FROM app_users
    WHERE EXISTS (
        SELECT 1
        FROM user_roles admin_role
        WHERE admin_role.user_id = app_users.id
          AND admin_role.role = 'ADMIN'
    )
      AND NOT EXISTS (
        SELECT 1
        FROM user_roles product_role
        WHERE product_role.user_id = app_users.id
          AND product_role.role IN ('STUDENT', 'MENTOR')
    )
);
