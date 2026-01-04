CREATE DATABASE IF NOT EXISTS libraryDatabase;
USE libraryDatabase;

-- Table for Books
CREATE TABLE IF NOT EXISTS books (
    id INT PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255),
    year INT,
    shelf VARCHAR(50)
);

-- Table for Members (Students)
CREATE TABLE IF NOT EXISTS members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE
);

-- Table for Issue Transactions
CREATE TABLE IF NOT EXISTS issues (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    member_id INT,
    issue_date DATE,
    return_date DATE DEFAULT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);

-- Optional Table for Librarian Credentials
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- Default Librarian Login
INSERT INTO users (username, password) VALUES ('admin', 'admin123');

INSERT INTO books (id, title, author, year, shelf) VALUES
(101, 'The Great Gatsby', 'F. Scott Fitzgerald', 1925, 'A1'),
(102, 'To Kill a Mockingbird', 'Harper Lee', 1960, 'A2'),
(103, '1984', 'George Orwell', 1949, 'A3'),
(104, 'Pride and Prejudice', 'Jane Austen', 1813, 'B1'),
(105, 'The Catcher in the Rye', 'J.D. Salinger', 1951, 'B2'),
(106, 'The Hobbit', 'J.R.R. Tolkien', 1937, 'B3'),
(107, 'Brave New World', 'Aldous Huxley', 1932, 'C1'),
(108, 'Moby Dick', 'Herman Melville', 1851, 'C2'),
(109, 'War and Peace', 'Leo Tolstoy', 1869, 'C3'),
(110, 'The Odyssey', 'Homer', -800, 'D1'),
(111, 'The Alchemist', 'Paulo Coelho', 1988, 'D2'),
(112, 'Crime and Punishment', 'Fyodor Dostoevsky', 1866, 'D3'),
(113, 'The Little Prince', 'Antoine de Saint-Exupéry', 1943, 'E1'),
(114, 'The Da Vinci Code', 'Dan Brown', 2003, 'E2'),
(115, 'Frankenstein', 'Mary Shelley', 1818, 'E3'),
(116, 'The Shining', 'Stephen King', 1977, 'F1'),
(117, 'The Handmaid\'s Tale', 'Margaret Atwood', 1985, 'F2'),
(118, 'A Brief History of Time', 'Stephen Hawking', 1988, 'F3'),
(119, 'The Book Thief', 'Markus Zusak', 2005, 'G1'),
(120, 'Life of Pi', 'Yann Martel', 2001, 'G2');


select * from books;
select * from issues;
select * from books where year='1988';
select * from members;
select * from users;