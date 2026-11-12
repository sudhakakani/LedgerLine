-- Optional: after registering the intended administrator through the UI,
-- replace the email below and run this as a database administrator.
UPDATE users SET role = 'ADMINISTRATOR' WHERE email = 'kakanisudha@example.com';
