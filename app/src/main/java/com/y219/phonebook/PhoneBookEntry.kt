package com.y219.phonebook

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_book_entries")
data class PhoneBookEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entry_id")
    val entryId: Long = 0,

    @ColumnInfo(
        name = "update_utc",
        defaultValue = "(CAST(strftime('%s', 'now') AS INTEGER) * 1000 " +
                "+ CAST(substr(strftime('%f', 'now'), 4, 3) AS INTEGER))"
    )
    val updateUtc: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "first_name")
    val firstName: String? = null,

    @ColumnInfo(name = "last_name")
    val lastName: String? = null,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,

    val email: String? = null,

    @ColumnInfo(name = "street_address")
    val streetAddress: String? = null,

    val apartment: String? = null,
    val city: String? = null,
    val state: String? = null,

    @ColumnInfo(name = "zip_code")
    val zipCode: String? = null,

    val note: String? = null
)