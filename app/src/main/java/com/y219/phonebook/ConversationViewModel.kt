package com.y219.phonebook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    data class State(val messages: List<String> = emptyList(), val busy: Boolean = false)

    private val dao = PhoneBookDatabase.getInstance(application).phoneBookDao()
    private val conversation = MutableStateFlow(State())
    val state = conversation.asStateFlow()

    fun restore(messages: List<String>) {
        if (conversation.value.messages.isEmpty()) conversation.value = State(messages)
    }

    fun submit(request: String) {
        val previous = conversation.value
        if (previous.busy || request.isBlank()) return
        val messages = previous.messages + "You: $request"
        conversation.value = State(messages, busy = true)
        viewModelScope.launch {
            val reply = try {
                replyTo(request)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                "The phone book could not complete your request. Please try again."
            }
            conversation.value = State(messages + "Phone Book: $reply")
        }
    }

    private suspend fun replyTo(request: String): String {
        val parts = request.split(Regex("\\s+"), limit = 4)
        return when (parts.first().lowercase()) {
            "find" -> {
                val name = parseCommandName(parts, 3) ?: return "Try Find Jane Smith."
                find(name)
            }
            "add" -> {
                val name = parseCommandName(parts, 4)
                val phone = parts.getOrNull(3)
                if (name == null || phone == null || phone.length > 100 ||
                    !phone.matches(Regex("""\+?[0-9(). \-]+""")) || phone.none(Char::isDigit)
                ) return "Try Add Jane Smith +1 202-555-0101."
                val id = dao.insert(
                    PhoneBookEntry(firstName = name.first, lastName = name.last, phoneNumber = phone)
                )
                "Added ${name.first} ${name.last} (ID $id)."
            }
            "edit", "delete" -> "Try Find Jane Smith to look up an entry's ID and phone number."
            "help" -> "Try Find Jane Smith, Add Jane Smith +1 202-555-0101, or What's Jane Smith's number?"
            else -> {
                if (request.length > 1_000) return "Enter a shorter lookup question or try Find Jane Smith."
                val name = try {
                    NameInterpreter.interpret(request)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    return "Natural-language lookup is unavailable. Use Find followed by the first and last name, for example Find Jane Smith."
                }
                if (name == null) {
                    "Whose number would you like? Ask again with their full name, or try Find Jane Smith."
                } else {
                    find(name)
                }
            }
        }
    }

    private fun parseCommandName(parts: List<String>, count: Int): ContactName? {
        if (parts.size != count) return null
        return ContactName.parse(parts[1], parts[2])
    }

    private suspend fun find(name: ContactName): String {
        val entries = dao.findByName(name.first, name.last)
        if (entries.isEmpty()) return "No entries found for ${name.first} ${name.last}."
        return entries.joinToString("\n") { entry ->
            "${entry.firstName} ${entry.lastName} (ID ${entry.entryId}) · ${entry.phoneNumber ?: "No phone number"}"
        }
    }
}
