UPDATE app_users
SET status = 'INACTIVE'
WHERE status IN ('BLOCKED', 'DELETED');
