-- Add inactive column to password_reset_token and invalidate previous tokens
ALTER TABLE password_reset_token ADD COLUMN inactive BOOLEAN;

UPDATE password_reset_token SET inactive = true;
