-- Authors
INSERT INTO Author (id, name, birthdate) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Stephen King',    '1947-09-21'),
    ('a1000000-0000-0000-0000-000000000002', 'J.R.R. Tolkien',  '1892-01-03'),
    ('a1000000-0000-0000-0000-000000000003', 'Philip K. Dick',  '1928-12-16');

-- Books — Stephen King
INSERT INTO Book (id, title, description, price, genre, author_id) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'The Shining',     'A family heads to an isolated hotel for the winter where a sinister presence influences the father into violence.',         1299, 'HORROR',   'a1000000-0000-0000-0000-000000000001'),
    ('b1000000-0000-0000-0000-000000000002', 'It',              'A group of children face an evil shapeshifting entity lurking in the sewers of their small Maine town.',                  1499, 'HORROR',   'a1000000-0000-0000-0000-000000000001'),
    ('b1000000-0000-0000-0000-000000000003', 'Misery',          'A famous novelist is held captive by an obsessive fan after a car accident in a remote area of Colorado.',                999,  'THRILLER', 'a1000000-0000-0000-0000-000000000001');

-- Books — Tolkien
INSERT INTO Book (id, title, description, price, genre, author_id) VALUES
    ('b1000000-0000-0000-0000-000000000004', 'The Fellowship of the Ring', 'A young hobbit embarks on a perilous quest to destroy a powerful ring and save Middle-earth from dark forces.',  1599, 'FANTASY',  'a1000000-0000-0000-0000-000000000002'),
    ('b1000000-0000-0000-0000-000000000005', 'The Two Towers',             'The fellowship is broken as the war of the ring spreads across Middle-earth in three separate storylines.',       1599, 'FANTASY',  'a1000000-0000-0000-0000-000000000002'),
    ('b1000000-0000-0000-0000-000000000006', 'The Return of the King',     'The final battle for Middle-earth unfolds as Frodo and Sam push into the dark land of Mordor.',                 1599, 'FANTASY',  'a1000000-0000-0000-0000-000000000002');

-- Books — Philip K. Dick
INSERT INTO Book (id, title, description, price, genre, author_id) VALUES
    ('b1000000-0000-0000-0000-000000000007', 'Do Androids Dream of Electric Sheep?', 'In a post-apocalyptic world a bounty hunter tracks down rogue androids indistinguishable from humans.', 1099, 'SCI_FI',  'a1000000-0000-0000-0000-000000000003'),
    ('b1000000-0000-0000-0000-000000000008', 'The Man in the High Castle',           'An alternate history where the Axis powers won World War II and divided the United States between them.',  999,  'SCI_FI',  'a1000000-0000-0000-0000-000000000003');
