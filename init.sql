CREATE TABLE products (
    id INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL,
    stock FLOAT8 NOT NULL
);

INSERT INTO products(id, name, stock) VALUES(1, 'p1', 2.0);
INSERT INTO products(id, name, stock) VALUES(2, 'p2', 10);
INSERT INTO products(id, name, stock) VALUES(3, 'p3', 100.0);