package com.example.speechrecognation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import com.microsoft.cognitiveservices.speech.*

class MainActivity : AppCompatActivity() {

    private val SPEECH_SUBSCRIPTION_KEY = "-"
    private val SPEECH_REGION = "northeurope"
    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 1

    private lateinit var recognizedTextView: TextView
    private lateinit var startRecognitionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recognizedTextView = findViewById(R.id.recognizedText)
        startRecognitionButton = findViewById(R.id.startRecognitionButton)

        // Request audio permissions
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_RECORD_AUDIO)
            return
        }

        startRecognitionButton.setOnClickListener {
            recognizeSpeech()
        }
    }

    private fun recognizeSpeech() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = SpeechConfig.fromSubscription(SPEECH_SUBSCRIPTION_KEY, SPEECH_REGION)
                val audioConfig = AudioConfig.fromDefaultMicrophoneInput()
                val recognizer = SpeechRecognizer(config, audioConfig)

                val result = recognizer.recognizeOnceAsync().get()
                withContext(Dispatchers.Main) {
                    when (result.reason) {
                        ResultReason.RecognizedSpeech -> recognizedTextView.text = result.text
                        ResultReason.NoMatch -> recognizedTextView.text = "No speech could be recognized."
                        ResultReason.Canceled -> {
                            val cancellation = CancellationDetails.fromResult(result)
                            recognizedTextView.text = "Recognition canceled: ${cancellation.errorDetails}"
                            Log.e("SpeechSDK", "ErrorCode: ${cancellation.errorCode}")
                        }

                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("SpeechSDK", "Error: ${e.message}")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startRecognitionButton.isEnabled = true
            } else {
                recognizedTextView.text = "Permission to access audio was denied"
            }
        }
    }
}