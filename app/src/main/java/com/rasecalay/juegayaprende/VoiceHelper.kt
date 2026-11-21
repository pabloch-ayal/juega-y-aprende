package com.rasecalay.juegayaprende

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

class VoiceHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                val localeEs = Locale("es", "PE")
                val res = engine.setLanguage(localeEs)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale("es", "ES"))
                }

                // Buscar voz femenina natural/neuronal disponible en el dispositivo
                try {
                    val voices = engine.voices
                    val sweetVoice = voices?.firstOrNull { v ->
                        v.locale.language == "es" &&
                        !v.isNetworkConnectionRequired &&
                        (v.name.contains("female", true) ||
                         v.name.contains("es-us-x-sfb", true) ||
                         v.name.contains("es-es-x-eef", true) ||
                         v.name.contains("neural", true))
                    } ?: voices?.firstOrNull { v ->
                        v.locale.language == "es" && !v.isNetworkConnectionRequired
                    }
                    if (sweetVoice != null) {
                        engine.voice = sweetVoice
                    }
                } catch (_: Exception) {}

                // Tono dulce, cálido y pausado tipo maestra de inicial
                engine.setPitch(1.18f)
                engine.setSpeechRate(0.92f)
                isReady = true
            }
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isReady) {
            onDone?.invoke()
            return
        }
        val engine = tts ?: return
        engine.stop()

        val utteranceId = UUID.randomUUID().toString()
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    onDone?.invoke()
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    onDone?.invoke()
                }
            }
        })

        val params = Bundle()
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun playSuccessSound() {
        try {
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 220)
        } catch (_: Exception) {}
    }

    fun playTapSound() {
        try {
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
        } catch (_: Exception) {}
    }

    fun playWrongSound() {
        try {
            toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 180)
        } catch (_: Exception) {}
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        toneGen.release()
    }
}
