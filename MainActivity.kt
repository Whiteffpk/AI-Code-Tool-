// MainActivity.kt
package com.example.aiassistant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputField: EditText = findViewById(R.id.inputField)
        val sendButton: Button = findViewById(R.id.sendButton)
        val outputView: TextView = findViewById(R.id.outputView)

        sendButton.setOnClickListener {
            val userInput = inputField.text.toString()
            callAIAPI(userInput) { response ->
                runOnUiThread {
                    outputView.text = response
                }
            }
        }
    }

    private fun callAIAPI(query: String, callback: (String) -> Unit) {
        val apiUrl = "https://api.openai.com/v1/completions" // Example API
        val jsonBody = """
            {
                "model": "text-davinci-003",
                "prompt": "$query",
                "max_tokens": 150
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(apiUrl)
            .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
            .addHeader("Authorization", "Bearer YOUR_API_KEY")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val aiResponse = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getString("text")
                    callback(aiResponse.trim())
                } else {
                    callback("No response from AI.")
                }
            }
        })
    }
}
