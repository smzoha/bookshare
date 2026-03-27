-- Allow anonymous activity
ALTER TABLE activity ALTER COLUMN login_id DROP NOT NULL;
