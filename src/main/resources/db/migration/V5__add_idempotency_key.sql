ALTER TABLE transactions
ADD COLUMN idempotency_key VARCHAR(100);

UPDATE transactions
SET idempotency_key = 'legacy-' || id
WHERE idempotency_key IS NULL;

ALTER TABLE transactions
ALTER COLUMN idempotency_key SET NOT NULL;

ALTER TABLE transactions
ADD CONSTRAINT uk_transactions_idempotency_key
UNIQUE (idempotency_key);