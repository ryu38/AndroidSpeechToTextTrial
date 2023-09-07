package com.example.speechtotexttrial

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.speechtotexttrial.ui.theme.SpeechToTextTrialTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var speechRecognizer: SpeechRecognizer? = null

    private var text by mutableStateOf("")
    private val statusLog = mutableStateListOf<String>()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            setSpeechRecognizer()
        } else {
            statusLog.add("$currentTime SpeechRecognizer isn't available")
        }
    }

//    private val requestOpenRecognizerIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//        if (it.resultCode == Activity.RESULT_OK) {
//            val recData = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//            if (recData == null) {
//                statusLog.add("$currentTime RecognizerIntent null")
//                return@registerForActivityResult
//            }
//            if (recData.size > 0) {
//                text = recData[0]
//            }
//        }
//    }

    private val speechRecognizerIntent by lazy {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPANESE)
        intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            setSpeechRecognizer()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            SpeechToTextTrialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView(text, statusLog, {
//                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//                        requestOpenRecognizerIntent.launch(intent)
                    })
                }
            }
        }
    }

    private fun setSpeechRecognizer() {
        val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
        recognizer.setRecognitionListener(object : RecognitionListener {

            private var isBusy = false

            override fun onError(error: Int) {
                statusLog.add("$currentTime Error code: $error")
            }
            override fun onResults(results: Bundle?) {
                val recData = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData == null) {
                    statusLog.add("$currentTime onResult null")
                    return
                }
                if (recData.size > 0) {
                    statusLog.add("$currentTime onResult received")
                    text = recData[0]
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val recData = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData == null) {
                    statusLog.add("$currentTime onPartialResults null")
                    return
                }
                if (recData.size > 0) {
                    statusLog.add("$currentTime onPartialResults received")
                    text = recData[0]
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {
                statusLog.add("$currentTime onReadyForSpeech")
            }
            override fun onBeginningOfSpeech() {
                statusLog.add("$currentTime onBeginningOfSpeech")
            }
            override fun onRmsChanged(rmsdB: Float) {
            }
            override fun onBufferReceived(buffer: ByteArray?) {
                statusLog.add("$currentTime onBufferReceived")
            }
            override fun onEndOfSpeech() {
                statusLog.add("$currentTime onEndOfSpeech")
                if (!isBusy) {
                    isBusy = true
                    speechRecognizer?.startListening(speechRecognizerIntent)
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        recognizer.startListening(speechRecognizerIntent)
        speechRecognizer = recognizer

//        val isReady = SpeechRecognizer.isOnDeviceRecognitionAvailable(this)
//        statusLog.add("SpeechRecognizer availability: $isReady")
    }

    val currentTime: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            return LocalDateTime.now().format(formatter)
        }
}

@Composable
fun MainView(text: String, logs: List<String>, onOpenRecognizerWindow: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text,
            fontSize = 20.sp,
        )
        Spacer(Modifier.height(16.dp))
//        Button(onClick = onOpenRecognizerWindow) {
//            Text("Open Recognizer Window")
//        }
        Spacer(Modifier.height(16.dp))
        logs.forEach {
            Text(it, color = Color.DarkGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewPreview() {
    SpeechToTextTrialTheme {
        MainView(text = "Hello", logs = listOf("No problems", "Have a nice day"), {})
    }
}