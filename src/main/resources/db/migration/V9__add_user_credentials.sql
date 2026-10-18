CREATE TABLE user_credentials(
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    CONSTRAINT pk_user_credentials
        PRIMARY KEY (user_id),

    CONSTRAINT fk_user_credentials_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
);