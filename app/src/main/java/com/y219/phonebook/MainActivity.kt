package com.y219.phonebook

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/** Displays the XML screen and handles the user's Send action. */
class MainActivity : AppCompatActivity() {

    // These references become available after setContentView creates the views.
    private lateinit var requestInput: EditText
    private lateinit var conversationArea: LinearLayout
    private lateinit var conversationScroll: ScrollView

    private lateinit var conversation: ConversationViewModel
    // Counts rows in this activity instance. A recreated screen starts with no rows
    // and renders the retained history without submitting those requests again.
    private var displayedMessages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PhoneBook)
        // Inflate res/layout/activity_main.xml and make it this activity's screen.
        // Always load the layout before looking up any of its view IDs.
        Log.d("MyTrace", "onCreate")
        setContentView(R.layout.activity_main)

        requestInput = findViewById(R.id.requestInput)
        conversationArea = findViewById(R.id.conversationArea)
        conversationScroll = findViewById(R.id.conversationScroll)
        val sendButton = findViewById<Button>(R.id.sendButton)

        // Keep content clear of system bars, display cutouts, and the keyboard.
        // Retain the XML padding and add the current insets to that original value.
        val root = conversationScroll.parent as View
        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val safeArea = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(
                left + safeArea.left, top + safeArea.top,
                right + safeArea.right, bottom + safeArea.bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)

        conversation = ViewModelProvider(this)[ConversationViewModel::class.java]
        savedInstanceState?.getStringArrayList("messages")?.let(conversation::restore)
        // Local command replies and replies produced after server-assisted lookups
        // arrive through the same state. Only append rows that are new to this screen.
        lifecycleScope.launch {
            conversation.state.collect { state ->
                val newMessages = state.messages.drop(displayedMessages)
                newMessages.forEach(::displayMessage)
                displayedMessages = state.messages.size
                sendButton.isEnabled = !state.busy

                // Wait for the new rows to be measured, then scroll once for the batch.
                // Scrolling directly leaves keyboard and accessibility focus in place.
                if (newMessages.isNotEmpty()) {
                    conversationScroll.post {
                        conversationScroll.scrollTo(0, conversationArea.bottom)
                    }
                }
            }
        }

        // A listener runs whenever the user taps Send.
        sendButton.setOnClickListener {
            val request = requestInput.text.toString().trim()

            // Do not create messages for empty or whitespace-only requests.
            if (request.isBlank()) {
                requestInput.error = "Enter a request first"
                return@setOnClickListener
            }

            // Check the current state in case another tap arrives before the button
            // reflects the busy state. Keep the draft when no request is accepted.
            if (conversation.state.value.busy) return@setOnClickListener
            conversation.submit(request)
            requestInput.text.clear()
        }
    }

override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main_menu, menu)
    Log.d("MyTrace", "Exit item visible: ${menu.findItem(R.id.action_exit)?.isVisible}")
    return true
}
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_exit) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun displayMessage(message: String) {
        // Requests and replies use the same row. The message text includes its sender.
        val messageView = TextView(this).apply {
            text = message
            textSize = 16f // TextView interprets this value in scalable pixels (sp).

            // setPadding expects pixels; convert 12dp using the screen density.
            val spacing = (12 * resources.displayMetrics.density).toInt()
            setPadding(0, spacing, 0, spacing)
        }
        conversationArea.addView(messageView)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save display text for Android to restore if the process is recreated.
        outState.putStringArrayList("messages", ArrayList(conversation.state.value.messages))
        super.onSaveInstanceState(outState)
    }
    override fun onStart() {
        super.onStart()
        Log.d("MyTrace", "onStart")
    }
    override fun onResume() {
        super.onResume()
        Log.d("MyTrace", "onResume")
    }
    override fun onPause() {
        super.onPause()
        Log.d("MyTrace", "onPause")
    }
    override fun onStop() {
        super.onStop()
        Log.d("MyTrace", "onStop")
    }
    override fun onRestart() {
        super.onRestart()
        Log.d("MyTrace", "onRestart")
    }
    override fun onDestroy() {
        Log.d("MyTrace", "onDestroy")
        super.onDestroy()
    }

}
