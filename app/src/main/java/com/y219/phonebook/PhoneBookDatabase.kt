package com.y219.phonebook

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PhoneBookEntry::class],
    version = 1,
    exportSchema = true
)
abstract class PhoneBookDatabase : RoomDatabase() {

    abstract fun phoneBookDao(): PhoneBookDao

    companion object {
        private var instance: PhoneBookDatabase? = null

        @Synchronized
        fun getInstance(context: Context): PhoneBookDatabase {
            return instance ?: Room.databaseBuilder(
                context.applicationContext,
                PhoneBookDatabase::class.java,
                "phonebook.db"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TRIGGER phone_book_entries_update_utc
                            AFTER UPDATE OF first_name, last_name, phone_number,
                                email, street_address, apartment, city, state,
                                zip_code, note
                            ON phone_book_entries
                            FOR EACH ROW
                            BEGIN
                                UPDATE phone_book_entries
                                SET update_utc =
                                    CAST(strftime('%s', 'now') AS INTEGER) * 1000
                                    + CAST(substr(strftime('%f', 'now'), 4, 3) AS INTEGER)
                                WHERE entry_id = NEW.entry_id;
                            END
                            """.trimIndent()
                        )
                    }
                })
                .build()
                .also { instance = it }
        }
    }
}