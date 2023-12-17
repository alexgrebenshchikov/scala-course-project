CREATE TABLE passwords (
    user_login text primary key,
    password_hash text,
    salt text
)