ALTER TABLE logins DROP COLUMN profile_picture_url;
ALTER TABLE logins ADD COLUMN profile_picture_id BIGINT;
ALTER TABLE logins ADD CONSTRAINT fk_login_profile_pic_image FOREIGN KEY (profile_picture_id) REFERENCES image (id);
