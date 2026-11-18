package com.y219.phonebook

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PhoneBookDao {
    @Insert
    suspend fun insert(entry: PhoneBookEntry): Long

    @Query(
        """
        SELECT * FROM phone_book_entries
        WHERE first_name = :firstName AND last_name = :lastName
        ORDER BY entry_id
        """
    )
    suspend fun findByName(
        firstName: String,
        lastName: String
    ): List<PhoneBookEntry>

    @Update
    suspend fun update(entry: PhoneBookEntry): Int

    @Query("DELETE FROM phone_book_entries WHERE entry_id = :entryId")
    suspend fun deleteById(entryId: Long): Int
}