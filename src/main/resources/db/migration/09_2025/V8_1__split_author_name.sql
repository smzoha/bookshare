ALTER TABLE author
ADD COLUMN first_name VARCHAR(255),
ADD COLUMN last_name VARCHAR(255);

UPDATE author
SET first_name = split_part(name, ' ', 1),
    last_name  = NULLIF(substring(name from position(' ' in name) + 1), '');

ALTER TABLE author DROP COLUMN name;
ALTER TABLE author ALTER COLUMN first_name SET NOT NULL;
