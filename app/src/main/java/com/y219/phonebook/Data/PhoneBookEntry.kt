package com.y219.phonebook.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents one contact row in the phone_book_entries SQLite table.
 *
 * The table and column names match Documentation/PhoneBook.dds.sql.
 * Room uses this class to map values between Kotlin objects and database
 * rows. Phone numbers and postal codes are strings so their formatting
 * and leading zeros are preserved.
 *
 * The database definition permits contact columns to be NULL. The app
 * should validate required values, such as a name and phone number, before
 * inserting a new contact.
 */
@Entity(tableName = "phone_book_entries")
data class PhoneBookEntry(

    /**
     * Unique identifier assigned to the row by SQLite.
     *
     * Room treats the default value of 0 as an unassigned ID when inserting
     * a new entity. The corresponding database column is entry_id.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entry_id")
    val entryId: Long = 0L,

    /**
     * Time the entry was added or last updated, in Unix epoch milliseconds.
     *
     * This is an absolute UTC-based timestamp, not a local-time string.
     * The SQL default matches the expression in PhoneBook.dds.sql. This
     * Kotlin default supplies a value when Room inserts the whole entity;
     * SQLite's column default applies when an insert omits the column.
     */
    @ColumnInfo(
        name = "update_utc",
        defaultValue = "(CAST(strftime('%s', 'now') AS INTEGER) * 1000 + CAST(substr(strftime('%f', 'now'), 4, 3) AS INTEGER))"
    )
    val updateUtc: Long = System.currentTimeMillis(),

    /** Contact's first name, stored in the first_name column. */
    @ColumnInfo(name = "first_name")
    val firstName: String? = null,

    /** Contact's last name, stored in the last_name column. */
    @ColumnInfo(name = "last_name")
    val lastName: String? = null,

    /**
     * Contact's phone number, stored as text to preserve formatting
     * and leading zeros.
     */
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,

    /** Optional email address. */
    @ColumnInfo(name = "email")
    val email: String? = null,

    /** Optional street address. */
    @ColumnInfo(name = "street_address")
    val streetAddress: String? = null,

    /** Optional apartment, unit, or suite information. */
    @ColumnInfo(name = "apartment")
    val apartment: String? = null,

    /** Optional city or locality. */
    @ColumnInfo(name = "city")
    val city: String? = null,

    /** Optional state, province, or region. */
    @ColumnInfo(name = "state")
    val state: String? = null,

    /**
     * Postal code stored as text so leading zeros and formatting
     * are preserved.
     */
    @ColumnInfo(name = "zip_code")
    val zipCode: String? = null,

    /** Optional notes about the contact. */
    @ColumnInfo(name = "note")
    val note: String? = null
)