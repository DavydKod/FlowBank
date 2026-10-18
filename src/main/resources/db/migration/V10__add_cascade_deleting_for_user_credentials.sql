ALTER TABLE user_credentials
DROP CONSTRAINT fk_user_credentials_user;

ALTER TABLE user_credentials
ADD CONSTRAINT fk_user_credentials_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;