-- Authors
INSERT INTO author (id, first_name, last_name, bio, created_at, updated_at)
VALUES (nextval('author_seq'), 'F. Scott', 'Fitzgerald',
        'American novelist widely regarded as one of the greatest writers of the 20th century, best known for The Great Gatsby.',
        NOW(), NOW()),
       (nextval('author_seq'), 'Harper', 'Lee',
        'American novelist who won the Pulitzer Prize for To Kill a Mockingbird in 1961.', NOW(), NOW()),
       (nextval('author_seq'), 'George', 'Orwell',
        'English novelist and essayist known for his sharp criticism of totalitarianism, most famously in 1984 and Animal Farm.',
        NOW(), NOW()),
       (nextval('author_seq'), 'J.R.R.', 'Tolkien',
        'English author and Oxford professor, creator of the mythological world of Middle-earth.', NOW(), NOW()),
       (nextval('author_seq'), 'Jane', 'Austen',
        'English novelist whose works of romantic fiction set among the British landed gentry earned her lasting fame.',
        NOW(), NOW()),
       (nextval('author_seq'), 'Frank', 'Herbert',
        'American science fiction author best known for the epic Dune saga, which reshaped the science fiction genre.',
        NOW(), NOW()),
       (nextval('author_seq'), 'J.D.', 'Salinger',
        'American author famous for his reclusive nature and the novel The Catcher in the Rye.', NOW(), NOW()),
       (nextval('author_seq'), 'J.K.', 'Rowling',
        'British author who created the Harry Potter series, one of the best-selling book series in history.', NOW(),
        NOW()),
       (nextval('author_seq'), 'Gabriel', 'García Márquez',
        'Colombian novelist and Nobel Prize laureate, pioneer of magical realism.', NOW(), NOW()),
       (nextval('author_seq'), 'Fyodor', 'Dostoevsky',
        'Russian novelist considered one of the greatest psychologists in world literature.', NOW(), NOW());

-- Tags
INSERT INTO tag (id, name, created_at, updated_at)
VALUES (nextval('tag_seq'), 'Classic', NOW(), NOW()),
       (nextval('tag_seq'), 'Bestseller', NOW(), NOW()),
       (nextval('tag_seq'), 'Award Winner', NOW(), NOW()),
       (nextval('tag_seq'), 'Dystopian', NOW(), NOW()),
       (nextval('tag_seq'), 'Coming of Age', NOW(), NOW()),
       (nextval('tag_seq'), 'Epic', NOW(), NOW()),
       (nextval('tag_seq'), 'Philosophical', NOW(), NOW()),
       (nextval('tag_seq'), 'Magical Realism', NOW(), NOW());

-- Genres
INSERT INTO genre (id, name, created_at)
VALUES (nextval('genre_seq'), 'Classic Literature',    NOW()),
       (nextval('genre_seq'), 'Philosophical Fiction', NOW()),
       (nextval('genre_seq'), 'Epic Fantasy',          NOW()),
       (nextval('genre_seq'), 'Space Opera',           NOW()),
       (nextval('genre_seq'), 'Gothic Fiction',        NOW());

-- Books
INSERT INTO book (id, title, isbn, description, pages, publication_date, status, created_at, updated_at)
VALUES (nextval('book_seq'), 'The Great Gatsby', '9780743273565',
        'A story of the fabulously wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan, set in the Roaring Twenties.',
        309, '1925-04-10', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'To Kill a Mockingbird', '9780061935466',
        'A Pulitzer Prize-winning masterpiece exploring racial injustice and moral growth in the American South through the eyes of young Scout Finch.',
        281, '1960-07-11', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'Nineteen Eighty-Four', '9780451524935',
        'A chilling vision of a totalitarian future where Big Brother watches every move and independent thought is a deadly crime.',
        328, '1949-06-08', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'The Hobbit', '9780547928227',
        'The extraordinary adventure of Bilbo Baggins, who joins a company of dwarves on a quest to reclaim their homeland from the dragon Smaug.',
        310, '1937-09-21', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'Pride and Prejudice', '9780141439518',
        'A beloved romantic novel following the witty Elizabeth Bennet as she navigates manners, morality, and marriage in Georgian England.',
        432, '1813-01-28', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'Dune', '9780441013593',
        'An epic science fiction saga set on the desert planet Arrakis, where control of the spice melange means control of the universe.',
        688, '1965-08-01', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'The Catcher in the Rye', '9780316769174',
        'A defining coming-of-age novel narrated by the cynical and rebellious Holden Caulfield as he wanders New York City after being expelled from school.',
        277, '1951-07-16', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'Harry Potter and the Philosopher''s Stone', '9780439708180',
        'The first book in the beloved Harry Potter series, following a young boy who discovers he is a wizard and enrols at Hogwarts School of Witchcraft.',
        309, '1997-06-26', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'One Hundred Years of Solitude', '9780060883287',
        'The multi-generational story of the Buendía family in the mythical town of Macondo, a landmark of magical realist literature.',
        417, '1967-05-30', 'ACTIVE', NOW(), NOW()),
       (nextval('book_seq'), 'The Brothers Karamazov', '9780374528379',
        'A profound philosophical novel exploring faith, doubt, free will, and morality through the story of three brothers and the murder of their father.',
        796, '1880-11-01', 'ACTIVE', NOW(), NOW());

-- Book / Author relationships
INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'F. Scott' AND a.last_name = 'Fitzgerald'
WHERE b.isbn = '9780743273565';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'Harper' AND a.last_name = 'Lee'
WHERE b.isbn = '9780061935466';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'George' AND a.last_name = 'Orwell'
WHERE b.isbn = '9780451524935';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'J.R.R.' AND a.last_name = 'Tolkien'
WHERE b.isbn = '9780547928227';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'Jane' AND a.last_name = 'Austen'
WHERE b.isbn = '9780141439518';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'Frank' AND a.last_name = 'Herbert'
WHERE b.isbn = '9780441013593';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'J.D.' AND a.last_name = 'Salinger'
WHERE b.isbn = '9780316769174';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'J.K.' AND a.last_name = 'Rowling'
WHERE b.isbn = '9780439708180';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'Gabriel' AND a.last_name = 'García Márquez'
WHERE b.isbn = '9780060883287';

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM book b
JOIN author a ON a.first_name = 'Fyodor' AND a.last_name = 'Dostoevsky'
WHERE b.isbn = '9780374528379';

-- Book / Tag relationships
INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id
FROM book b
JOIN tag t ON t.name = 'Classic'
WHERE b.isbn IN ('9780743273565', '9780451524935', '9780547928227', '9780141439518', '9780374528379');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id
FROM book b
JOIN tag t ON t.name = 'Bestseller'
WHERE b.isbn IN ('9780061935466', '9780547928227', '9780439708180', '9780060883287');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id
FROM book b
JOIN tag t ON t.name = 'Award Winner'
WHERE b.isbn IN ('9780061935466', '9780060883287');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id
FROM book b
JOIN tag t ON t.name = 'Dystopian'
WHERE b.isbn IN ('9780451524935');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id
FROM book b
JOIN tag t ON t.name = 'Coming of Age'
WHERE b.isbn IN ('9780743273565', '9780316769174', '9780061935466', '9780439708180');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id
FROM book b
JOIN tag t ON t.name = 'Epic'
WHERE b.isbn IN ('9780547928227', '9780441013593', '9780374528379');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id
FROM book b
JOIN tag t ON t.name = 'Philosophical'
WHERE b.isbn IN ('9780374528379', '9780451524935', '9780060883287');

INSERT INTO book_tags (book_id, tag_id)
SELECT b.id, t.id
FROM book b
JOIN tag t ON t.name = 'Magical Realism'
WHERE b.isbn IN ('9780060883287');

-- Book / Genre relationships (lookup genres by name to avoid hardcoded IDs)
INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Literary Fiction'
WHERE b.isbn = '9780743273565';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Contemporary Fiction'
WHERE b.isbn = '9780061935466';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Post-Apocalyptic / Dystopian'
WHERE b.isbn = '9780451524935';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Fantasy'
WHERE b.isbn = '9780547928227';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Romance'
WHERE b.isbn = '9780141439518';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Science Fiction'
WHERE b.isbn = '9780441013593';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Literary Fiction'
WHERE b.isbn = '9780316769174';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Young Adult (YA)'
WHERE b.isbn = '9780439708180';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Magical Realism'
WHERE b.isbn = '9780060883287';

INSERT INTO book_genre (book_id, genre_id)
SELECT b.id, g.id
FROM book b
JOIN genre g ON g.name = 'Literary Fiction'
WHERE b.isbn = '9780374528379';
