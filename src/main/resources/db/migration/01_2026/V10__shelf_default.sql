ALTER TABLE shelf ADD COLUMN default_shelf BOOLEAN DEFAULT FALSE;

UPDATE shelf
SET default_shelf = TRUE
WHERE name IN ('Currently Reading', 'Want to Read', 'Read');

ALTER TABLE shelf ALTER COLUMN default_shelf SET NOT NULL;
