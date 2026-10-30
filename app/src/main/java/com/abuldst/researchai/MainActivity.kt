package com.abuldst.researchai

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Backend

class MainActivity : Activity() {

    private val executor = Executors.newSingleThreadExecutor()

    private var engine: Engine? = null
    private var conversation: com.google.ai.edge.litertlm.Conversation? = null

    private lateinit var status: TextView
    private lateinit var output: TextView
    private lateinit var question: EditText
    private lateinit var loadButton: Button
    private lateinit var askButton: Button

    private var modelFile: File? = null

    private val PICK_MODEL = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 30, 24, 20)
            setBackgroundColor(Color.rgb(18, 22, 32))
        }

        fun addText(text: String, size: Float): TextView {
            return TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(Color.WHITE)
                setPadding(0, 12, 0, 12)
                root.addView(this)
            }
        }

        addText("Abul Research AI", 26f)
        addText("Your private, offline research assistant", 15f)

        status = addText("Choose your local AI model to begin.", 14f)

        val chooseButton = Button(this).apply {
            text = "Choose AI Model"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                startActivityForResult(intent, PICK_MODEL)
            }
        }
        root.addView(chooseButton)

        loadButton = Button(this).apply {
            text = "Load AI Model"
            isEnabled = false
            setOnClickListener { loadModel() }
        }
        root.addView(loadButton)

        question = EditText(this).apply {
            hint = "Enter your research question..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(12, 12, 12, 12)
        }
        root.addView(question)

        askButton = Button(this).apply {
            text = "Start Research"
            isEnabled = false
            setOnClickListener { askAI() }
        }
        root.addView(askButton)

        output = addText("Your AI research answers will appear here.", 16f)
root.removeView(output)
val scroll = ScrollView(this)
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MODEL &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {
            val uri = data.data!!

            status.text = "Model selected. Ready to load."
            loadButton.isEnabled = true

            modelUri = uri
        }
    }

    private var modelUri: Uri? = null

    private fun loadModel() {
    val uri = modelUri ?: return

    loadButton.isEnabled = false
    askButton.isEnabled = false
    status.text = "Checking and copying Gemma model..."

    executor.execute {
        try {
            val expectedSize = 2588147712L
            val file = File(filesDir, "gemma-4-E2B-it.litertlm")
            val tempFile = File(filesDir, "gemma-model.tmp")

            if (file.exists() && file.length() != expectedSize) {
                file.delete()
            }

            if (!file.exists()) {
                tempFile.delete()

                contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) {
                        "Cannot open selected model."
                    }

                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output, 1024 * 1024)
                    }
                }

                require(tempFile.length() == expectedSize) {
                    "Model size mismatch. Expected 2588147712 bytes, got ${tempFile.length()}."
                }

                require(tempFile.renameTo(file)) {
                    "Could not save model file."
                }
            }

            require(file.length() == expectedSize) {
                "Model file size is incorrect."
            }

            runOnUiThread {
                status.text = "Verified. Loading Gemma AI..."
            }

            val newEngine = Engine(
                EngineConfig(
                    modelPath = file.absolutePath,
                    backend = Backend.CPU(),
                    cacheDir = cacheDir.absolutePath
                )
            )

            newEngine.initialize()

            val newConversation = newEngine.createConversation()

            engine = newEngine
            conversation = newConversation
            modelFile = file

            runOnUiThread {
                status.text = "AI READY! Gemma is loaded successfully."
                askButton.isEnabled = true
                loadButton.isEnabled = false
            }

        } catch (e: Exception) {
            runOnUiThread {
                status.text = "Loading failed: ${e.message}"
                loadButton.isEnabled = true
            }
        }
    }
    }
        }
    
    private fun askAI() {
        val query = question.text.toString().trim()

        if (query.isEmpty()) {
            question.error = "Enter a question first"
            return
        }

        askButton.isEnabled = false
        output.text = "Researching... Please wait."

        executor.execute {
            try {
                val response = conversation?.sendMessage(query)

                runOnUiThread {
                    output.text = response?.toString() ?: "No response received."
                    askButton.isEnabled = true
                }

            } catch (e: Exception) {
                runOnUiThread {
                    output.text = "Research error: ${e.message}"
                    askButton.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {
        executor.execute {
            conversation?.close()
            conversation = null

            engine?.close()
            engine = null
        }

        executor.shutdown()
        super.onDestroy()
    }
}
