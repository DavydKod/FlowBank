ALTER TABLE bank_accounts
ADD COLUMN daily_transfer_limit NUMERIC(19, 2)
NOT NULL DEFAULT 1000.00;

ALTER TABLE bank_accounts
ALTER COLUMN daily_transfer_limit DROP DEFAULT;

ALTER TABLE bank_accounts
ADD CONSTRAINT chk_daily_transfer_limit_positive
CHECK (daily_transfer_limit > 0);