ALTER TABLE shelf ADD CONSTRAINT uk_shelf_name_login UNIQUE (name, login_id);
ALTER TABLE shelved_book ADD CONSTRAINT uk_shelved_book_shelf_login_book UNIQUE (shelf_id, book_id, user_id);
