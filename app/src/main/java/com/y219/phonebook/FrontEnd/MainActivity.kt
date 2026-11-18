package com.y219.phonebook.frontend
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.y219.phonebook.R
import android.view.Menu
import android.view.MenuItem
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Displays the XML screen and handles the user's Send action. */
class MainActivity : AppCompatActivity() {
    // These references become available after setContentView creates the views.
    private lateinit var requestInput: EditText
    private lateinit var conversationArea: LinearLayout
    private lateinit var conversationScroll: ScrollView
    // Keep messages so they can survive ativity recreation.
    // This is temporary screen state, not a permanent contact database.
    private val messages = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        Log.d("MyTrace", "onCreate")
        // Load the XML layout before looking up any of its views.
        setContentView(R.layout.activity_main)
        // Connect Kotlin variables to the IDs defined in activity_main.xml.
        requestInput = findViewById(R.id.requestInput)
        conversationArea = findViewById(R.id.conversationArea)
        conversationScroll = findViewById(R.id.conversationScroll)
        val sendButton = findViewById<Button>(R.id.sendButton)
        // Keep content clear of system bars, display cutouts, and the keyboard.
        //Save the original XML padding so repeated updates do not accumulate it.
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
        // Restore messages after activity recreation, such as screen rotation.
        // Android restores the input field's state using its stable XML ID.
        savedInstanceState?.getStringArrayList("messages")?.let {
            messages.addAll(it)
        }
        messages.forEach { displayMessage(it) }
        // Run this listener whenever the user taps Send.
        val phoneBookViewModel = ViewModelProvider(this)[PhoneBookViewModel::class.java]
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                phoneBookViewModel.replies.collect { reply ->
                    addMessage("Phone Book: $reply")
                }
            }
        }

        sendButton.setOnClickListener {
            val request = requestInput.text.toString().trim()
            // Ignore empty requests and show an error beside the input field.
            if (request.isBlank()) {
                requestInput.error = "Enter a request first"
                return@setOnClickListener
            }
            addMessage("You: $request")
            requestInput.text.clear()
            // Temporary reply: replace this with contact-management logic.
            // This example does not read, add, or change contacts.
            phoneBookViewModel.submit(request)
        }
    }

    private fun addMessage(message: String) {
        // Save the message, then create the view that displays it.
        messages.add(message)
        displayMessage(message)
    }

    private fun displayMessage(message: String) {
        // Create one TextView for each message in the conversation.
        val messageView = TextView(this).apply {
            text = message
            textSize = 16f // Text size uses scalable pixels (sp).
            // Padding requires pixels, so convert 12dp using screen density.
            val spacing = (12 * resources.displayMetrics.density).toInt()
            setPadding(0, spacing, 0, spacing)
        }
        conversationArea.addView(messageView)
        // Wait until Android lays out the new message before scrolling.
        conversationScroll.post {
            conversationScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Preserve this small conversation when Android recreates the activity.
        // A growing production history should use persistent storage instead.
        outState.putStringArrayList("messages", ArrayList(messages))
        super.onSaveInstanceState(outState)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                    true
            }
            R.id.action_exit -> {
                finishAndRemoveTask()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
