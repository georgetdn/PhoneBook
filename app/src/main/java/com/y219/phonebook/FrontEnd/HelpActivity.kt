package com.y219.phonebook.frontend

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.y219.phonebook.R

/**
 * Displays the app's usage guide.
 *
 * The guide is stored as a bundled HTML asset so its headings, paragraphs,
 * and lists can be maintained separately from the Activity implementation.
 * HtmlCompat converts the static HTML into styled text for the TextView.
 */
class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the screen containing the scrollable help text.
        setContentView(R.layout.activity_help)

        // Read the bundled guide using UTF-8 so punctuation is decoded correctly.
        val helpHtml = assets.open("help.html")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        // Convert supported HTML formatting into displayable styled text.
        findViewById<TextView>(R.id.helpContent).text =
            HtmlCompat.fromHtml(
                helpHtml,
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
    }
}