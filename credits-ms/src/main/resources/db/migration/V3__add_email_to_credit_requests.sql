ALTER TABLE credit_requests
    ADD COLUMN email VARCHAR(255) NOT NULL DEFAULT 'unknown@example.com';

ALTER TABLE credit_requests
    ALTER COLUMN email DROP DEFAULT;
