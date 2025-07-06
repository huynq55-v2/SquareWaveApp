package com.example.squarewave

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private var audioThread: Thread? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener {
            val freq = etFrequency.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            startSquareWave(freq)
            btnStart.isEnabled = false
            btnStop.isEnabled = true
        }

        btnStop.setOnClickListener {
            stopSquareWave()
            btnStart.isEnabled = true
            btnStop.isEnabled = false
        }
    }

    private fun startSquareWave(freqHz: Double) {
        if (isPlaying) return
        isPlaying = true
        audioThread = thread {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_OUT_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            val track = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufSize,
                AudioTrack.MODE_STREAM
            )
            track.play()

            val samplesPerCycle = (sampleRate / freqHz).toInt().coerceAtLeast(1)
            val bufferSamples = bufSize / 2  // Short = 2 bytes
            val buffer = ShortArray(bufferSamples)
            var phase = 0

            while (isPlaying) {
                for (i in buffer.indices) {
                    buffer[i] = if (phase < samplesPerCycle / 2) Short.MAX_VALUE else Short.MIN_VALUE
                    phase = (phase + 1) % samplesPerCycle
                }
                track.write(buffer, 0, buffer.size)
            }

            track.stop()
            track.release()
        }
    }

    private fun stopSquareWave() {
        isPlaying = false
        audioThread?.join()
        audioThread = null
    }
}
