package com.y219.phonebook.frontend
import android.app.Application
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.y219.phonebook.Data.PhoneBookDatabase
import com.y219.phonebook.frontend.PhoneBookRequestProcessor

class PhoneBookViewModel(application: Application) : AndroidViewModel(application) {
    private val processor by lazy {
        val dao = PhoneBookDatabase.getInstance(application).phoneBookDao()
        PhoneBookRequestProcessor(dao)
    }
    private val replyChannel = Channel<String>(Channel.BUFFERED)
    val replies = replyChannel.receiveAsFlow()
    fun submit(request: String) {
        viewModelScope.launch {
            val reply = try {
                processor.process(request)
            } catch (error: SQLiteException) {
                Log.e("PhoneBook", "Phone book lookup failed", error)
                "The phone book could not be read. Please try again."
            }

            replyChannel.send(reply)
        }
    }
}

