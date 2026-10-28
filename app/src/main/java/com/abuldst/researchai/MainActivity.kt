package com.abuldst.researchai

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

private lateinit var output: TextView
private lateinit var question: EditText

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 32, 24, 24)
        setBackgroundColor(Color.rgb(12, 18, 32))
    }

    val title = TextView(this).apply {
        text = "Abul Research AI"
        textSize = 26f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
    }
    root.addView(title)

    val subtitle = TextView(this).apply {
        text = "Your private, offline research assistant"
        textSize = 14f
        setTextColor(Color.LTGRAY)
        gravity = Gravity.CENTER
        setPadding(0, 8, 0, 24)
    }
    root.addView(subtitle)

    question = EditText(this).apply {
        hint = "Enter a research question..."
        textSize = 16f
        setTextColor(Color.WHITE)
        setHintTextColor(Color.LTGRAY)
        setPadding(16, 12, 16, 12)
    }
    root.addView(
        question,
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    )

    val askButton = Button(this).apply {
        text = "Start Research"
        setOnClickListener { answerQuestion() }
    }
    root.addView(askButton)

    val scroll = ScrollView(this)
    output = TextView(this).apply {
        text = "Welcome!\n\nAbul Research AI is being built. " +
            "The local AI engine has not been connected yet."
        textSize = 16f
        setTextColor(Color.WHITE)
        setPadding(0, 24, 0, 0)
    }
    scroll.addView(output)
    root.addView(
        scroll,
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
    )

    setContentView(root)
}

private fun answerQuestion() {
    val query = question.text.toString().trim()

    output.text = if (query.isEmpty()) {
        "Please enter a research question."
    } else {
        "Your question:\n$query\n\n" +
            "The offline AI engine is not connected yet. " +
            "This interface currently works as a prototype; " +
            "it does not generate AI research answers yet."
    }
}

}
