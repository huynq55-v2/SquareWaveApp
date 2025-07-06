package com.example.squarewaveapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import com.example.squarewaveapp.R

class MainActivity : AppCompatActivity() {
    companion object {
        init { System.loadLibrary("native-lib") }
    }

    private external fun nativeStart(frequency: Double)
    private external fun nativeStop()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val freqInput = findViewById<EditText>(R.id.frequencyInput)
        val startBtn = findViewById<Button>(R.id.startButton)
        val stopBtn = findViewById<Button>(R.id.stopButton)

        startBtn.setOnClickListener {
            val freq = freqInput.text.toString().toDoubleOrNull() ?: 440.0
            nativeStart(freq)
            startBtn.isEnabled = false
            stopBtn.isEnabled = true
        }

        stopBtn.setOnClickListener {
            nativeStop()
            startBtn.isEnabled = true
            stopBtn.isEnabled = false
        }
    }
}