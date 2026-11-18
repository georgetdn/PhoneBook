package com.y219.phonebook.frontend
import com.y219.phonebook.Data.PhoneBookEntryDao

class PhoneBookRequestProcessor(private val dao: PhoneBookEntryDao) {
    private val findCommand = Regex(
        """find\s+(\S+)\s+(\S+)""",
        RegexOption.IGNORE_CASE
    )

    suspend fun process(request: String): String {
        val match = findCommand.matchEntire(request)
            ?: return "Enter a first and last name, for example: Find Jane Smith."

        val (firstName, lastName) = match.destructured
        val entries = dao.findByName(firstName, lastName)

        if (entries.isEmpty()) {
            return "No entries found for $firstName $lastName."
        }

        val lines = entries.joinToString(separator = "\n") { entry ->
                        val number = entry.phoneNumber
                            ?.takeIf { it.isNotBlank() }
                            ?: "No phone number on file"

                        "ID ${entry.entryId}: $number"
        }
        return "$firstName $lastName (${entries.size} found)\n$lines"
    }
}