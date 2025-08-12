-- Insert sample categories
INSERT INTO categories (name, description, created_at, updated_at) VALUES
('Fiction', 'Fictional literature including novels and short stories', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Science', 'Scientific books and research publications', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Technology', 'Technology, programming, and computer science books', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('History', 'Historical books and biographies', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Philosophy', 'Philosophy and ethics books', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Business', 'Business, economics, and management books', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample users
INSERT INTO users (moodle_id, name, email, role, date_of_birth, address, is_active, created_at, updated_at) VALUES
('user001', 'John Doe', 'john.doe@email.com', 'MEMBER', '1990-05-15', '123 Main St, City, State', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user002', 'Jane Smith', 'jane.smith@email.com', 'MEMBER', '1985-08-22', '456 Oak Ave, City, State', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user003', 'Bob Johnson', 'bob.johnson@email.com', 'MEMBER', '1992-03-10', '789 Pine Rd, City, State', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user004', 'Alice Brown', 'alice.brown@email.com', 'LIBRARIAN', '1988-12-01', '321 Elm St, City, State', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user005', 'Charlie Wilson', 'charlie.wilson@email.com', 'ADMIN', '1980-11-15', '654 Maple Dr, City, State', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample books
INSERT INTO books (name, author, isbn, publication_year, total_copies, available_copies, category_id, short_details, about, format, created_at, updated_at, book_cover_url, pdf_file_url, audio_file_url) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', '978-0-7432-7356-5', 1925, 5, 3, 1, 'A classic American novel', 'A classic American novel set in the Jazz Age', 'HARD_COPY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/commons/7/7a/The_Great_Gatsby_Cover_1925_Retouched.jpg', null, null),
('To Kill a Mockingbird', 'Harper Lee', '978-0-06-112008-4', 1960, 4, 2, 1, 'A novel about racial injustice', 'A novel about racial injustice in the American South', 'HARD_COPY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/commons/4/4f/To_Kill_a_Mockingbird_%28first_edition_cover%29.jpg', null, null),
('A Brief History of Time', 'Stephen Hawking', '978-0-553-38016-3', 1988, 3, 3, 2, 'A popular science book', 'A popular science book about cosmology', 'HARD_COPY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/en/a/a3/BriefHistoryTime.jpg', null, null),
('Clean Code', 'Robert C. Martin', '978-0-13-235088-4', 2008, 6, 4, 3, 'A handbook of agile software', 'A handbook of agile software craftsmanship', 'E_BOOK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://www.oreilly.com/covers/urn:orm:book:9780136083238/400w/', null, null),
('The Art of War', 'Sun Tzu', '978-0-14-044919-4', 500, 2, 1, 4, 'Ancient Chinese military treatise', 'Ancient Chinese military treatise', 'HARD_COPY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/commons/2/2a/Bamboo_book_-_closed_-_UCR.jpg', null, null),
('Thinking, Fast and Slow', 'Daniel Kahneman', '978-0-374-53355-7', 2011, 4, 4, 5, 'A book about behavioral psychology', 'A book about behavioral psychology and decision-making', 'E_BOOK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/en/c/c1/Thinking%2C_Fast_and_Slow.jpg', null, null),
('Good to Great', 'Jim Collins', '978-0-06-662099-2', 2001, 3, 2, 6, 'A management book', 'A management book about company transformation', 'HARD_COPY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/en/0/03/Cover_Good_2_Gr8.jpg', null, null),
('1984', 'George Orwell', '978-0-452-28423-4', 1949, 5, 3, 1, 'A dystopian social science fiction', 'A dystopian social science fiction novel', 'HARD_COPY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/commons/5/51/1984_first_edition_cover.jpg', null, null),
('The Origin of Species', 'Charles Darwin', '978-0-14-043205-9', 1859, 2, 2, 2, 'A work of scientific literature', 'A work of scientific literature on evolution', 'HARD_COPY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/commons/c/cd/Origin_of_Species_title_page.jpg', null, null),
('Design Patterns', 'Gang of Four', '978-0-201-63361-0', 1994, 4, 3, 3, 'Elements of reusable software', 'Elements of reusable object-oriented software', 'E_BOOK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'https://upload.wikimedia.org/wikipedia/en/7/78/Design_Patterns_cover.jpg', null, null);


-- Insert sample borrows (some active, some returned)
INSERT INTO borrows (user_id, book_id, borrow_date, due_date, return_date, status, extension_count, created_at, updated_at) VALUES
(1, 1, '2024-01-10', '2024-01-24', '2024-01-22', 'RETURNED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, '2024-01-15', '2024-01-29', NULL, 'ACTIVE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 4, '2024-01-20', '2024-02-03', NULL, 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 5, '2024-01-25', '2024-02-08', NULL, 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 7, '2024-01-12', '2024-01-26', '2024-01-25', 'RETURNED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, '2024-01-30', '2024-02-13', NULL, 'ACTIVE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert admin settings with new structure
INSERT INTO admin_settings (borrow_day_limit, borrow_extend_limit, borrow_book_limit, booking_days_limit, created_at, updated_at) VALUES
(14, 2, 5, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

