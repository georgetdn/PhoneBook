-- PhoneBook SQLite data definition script, version 1.
-- update_utc stores milliseconds since the Unix epoch.
BEGIN;

CREATE TABLE phone_book_entries (
    entry_id INTEGER PRIMARY KEY,
    update_utc INTEGER NOT NULL DEFAULT (
        CAST(strftime('%s', 'now') AS INTEGER) * 1000
        + CAST(substr(strftime('%f', 'now'), 4, 3) AS INTEGER)
    ),
    first_name TEXT,
    last_name TEXT,
    phone_number TEXT,
    email TEXT,
    street_address TEXT,
    apartment TEXT,
    city TEXT,
    state TEXT,
    zip_code TEXT,
    note TEXT
);

CREATE TRIGGER phone_book_entries_update_utc
AFTER UPDATE OF first_name, last_name, phone_number, email, street_address,
    apartment, city, state, zip_code, note
ON phone_book_entries
FOR EACH ROW
BEGIN
    UPDATE phone_book_entries
    SET update_utc = CAST(strftime('%s', 'now') AS INTEGER) * 1000
        + CAST(substr(strftime('%f', 'now'), 4, 3) AS INTEGER)
    WHERE entry_id = NEW.entry_id;
END;

COMMIT;
