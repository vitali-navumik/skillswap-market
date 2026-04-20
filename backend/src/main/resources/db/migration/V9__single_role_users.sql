DELETE FROM user_roles product_role
WHERE product_role.role IN ('STUDENT', 'MENTOR')
  AND EXISTS (
      SELECT 1
      FROM user_roles admin_role
      WHERE admin_role.user_id = product_role.user_id
        AND admin_role.role = 'ADMIN'
  );

DELETE FROM user_roles student_role
WHERE student_role.role = 'STUDENT'
  AND EXISTS (
      SELECT 1
      FROM user_roles mentor_role
      WHERE mentor_role.user_id = student_role.user_id
        AND mentor_role.role = 'MENTOR'
  );
