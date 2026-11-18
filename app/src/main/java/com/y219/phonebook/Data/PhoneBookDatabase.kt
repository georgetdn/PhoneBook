package com.y219.phonebook.Data

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
/**
 * The Room database definition for the Phone Book app.
 *
 * The @Database annotation tells Room which entity classes make up this
 * database and identifies its current schema version. Room uses this
 * definition, together with the entity and DAO classes, to generate the
 * database implementation.
 *
 * Keep the version at 1 while the database structure remains unchanged.
 * When the structure changes in a later app version, increase the version
 * and provide the required migration so existing contact data is preserved.
 */
@Database(
    entities = [
        PhoneBookEntry::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PhoneBookDatabase : RoomDatabase() {

    /**
     * Provides access to the SQL operations defined by PhoneBookEntryDao.
     *
     * This function is abstract because Room generates its implementation.
     * Call it on a PhoneBookDatabase instance to obtain the DAO used to
     * find, insert, update, and delete phone book entries.
     */
    abstract fun phoneBookDao(): PhoneBookEntryDao
    /**
    +     * Creates the database the first time it is requested and reuses that
    +     * instance for later requests in this app process.
    +     */
        companion object {
              @Volatile
              private var INSTANCE: PhoneBookDatabase? = null

              /**
               * Returns the shared database instance.
               *
               * applicationContext is used so the database does not keep an
               * Activity or other short-lived Context alive.
               */
              fun getInstance(context: Context): PhoneBookDatabase {
                        return INSTANCE ?: synchronized(this) {
                                INSTANCE ?: Room.databaseBuilder(
                                        context.applicationContext,
                                        PhoneBookDatabase::class.java,
                                        DATABASE_NAME
                                            ).build().also { database ->
                                        INSTANCE = database
                                    }
                            }
                    }

                private const val DATABASE_NAME = "phone_book_database"
            }
}
