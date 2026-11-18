package com.y219.phonebook.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Defines the database operations available for phone book entries.
 *
 * The SQL table is named phone_book_entries. Each method here describes
 * an operation; Room generates the implementation when the project builds.
 *
 * The caller should invoke these suspend functions from a coroutine.
 * The DAO does not decide when to ask the user for deletion confirmation;
 * that belongs in the user-interface or command-handling layer.
 */
@Dao
interface PhoneBookEntryDao {

    /**
     * Returns every saved entry, ordered by name and then by ID.
     *
     * Sorting names without regard to case makes the result list easier to
     * scan. The ID provides a stable order when entries have the same name.
     */
    @Query(
        """
        SELECT * FROM phone_book_entries
        ORDER BY last_name COLLATE NOCASE,
                 first_name COLLATE NOCASE,
                 entry_id
        """
    )
    suspend fun getAllEntries(): List<PhoneBookEntry>

    /**
     * Finds every entry whose first and last names match the supplied names.
     *
     * A list is returned because the database allows multiple entries to
     * have the same name. The caller should display every match, including
     * its ID and phone number, so the user can identify the right entry.
     */
    @Query(
        """
        SELECT * FROM phone_book_entries
        WHERE first_name = :firstName COLLATE NOCASE
          AND last_name = :lastName COLLATE NOCASE
        ORDER BY entry_id
        """
    )
    suspend fun findByName(
        firstName: String,
        lastName: String
    ): List<PhoneBookEntry>

    /**
     * Looks up one entry by its unique database ID.
     *
     * The result is null when no row has that ID. Edit and delete workflows
     * can use this method to confirm which saved contact the ID identifies.
     */
    @Query(
        """
        SELECT * FROM phone_book_entries
        WHERE entry_id = :entryId
        LIMIT 1
        """
    )
    suspend fun findById(entryId: Long): PhoneBookEntry?

    /**
     * Inserts one entry and returns the row ID assigned by SQLite.
     *
     * ABORT prevents an insert conflict from silently replacing another
     * record. The caller should report success only after this function
     * returns successfully.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PhoneBookEntry): Long

    /**
     * Changes only the name and phone number for the selected entry.
     *
     * All other columns are left as they are. The database definition's
     * update trigger updates update_utc when one of these contact fields
     * changes. The returned count is 1 when an entry was updated and 0
     * when the supplied ID does not exist.
     */
    @Query(
        """
        UPDATE phone_book_entries
        SET first_name = :firstName,
            last_name = :lastName,
            phone_number = :phoneNumber
        WHERE entry_id = :entryId
        """
    )
    suspend fun updateNameAndPhone(
        entryId: Long,
        firstName: String,
        lastName: String,
        phoneNumber: String
    ): Int

    /**
     * Deletes the entry with the specified ID.
     *
     * This method does not request confirmation. The calling layer should
     * identify the entry and get the user's confirmation before invoking it.
     * The returned count is 1 if a row was removed, or 0 if the ID was absent.
     */
    @Query(
        """
        DELETE FROM phone_book_entries
        WHERE entry_id = :entryId
        """
    )
    suspend fun deleteById(entryId: Long): Int
}