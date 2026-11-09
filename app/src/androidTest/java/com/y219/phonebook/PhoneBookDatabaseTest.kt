package com.y219.phonebook

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneBookDatabaseTest {

    @Test
    fun insertEntryAndFindItByName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context,
            PhoneBookDatabase::class.java
        ).build()

        try {
            runBlocking {
                val dao = database.phoneBookDao()
                val entryId = dao.insert(
                    PhoneBookEntry(
                        firstName = "Jane",
                        lastName = "Smith",
                        phoneNumber = "+1 202-555-0101"
                    )
                )

                val matches = dao.findByName("Jane", "Smith")

                assertTrue(entryId > 0)
                assertEquals(1, matches.size)
                assertEquals(entryId, matches[0].entryId)
                assertEquals("+1 202-555-0101", matches[0].phoneNumber)
                assertTrue(dao.findByName("Jane", "Jones").isEmpty())
            }
        } finally {
            database.close()
        }
    }
}