package com.rasecalay.juegayaprende

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale
import java.util.UUID

class VoiceHelper(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
    private var spanishVoices: List<Voice> = emptyList()
    private var currentVoiceIndex = 0

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                val res = engine.setLanguage(Locale("es", "US"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale("es", "ES"))
                }

                try {
                    spanishVoices = (engine.voices?.filter {
                        it.locale.language == "es" && !it.isNetworkConnectionRequired
                    } ?: emptyList()).sortedBy { it.name }

                    val prefs = appContext.getSharedPreferences("juega_aprende_prefs", Context.MODE_PRIVATE)
                    val savedName = prefs.getString("saved_voice_name", null)
                    val savedIdx = spanishVoices.indexOfFirst { it.name == savedName }

                    if (savedIdx >= 0) {
                        currentVoiceIndex = savedIdx
                        engine.voice = spanishVoices[currentVoiceIndex]
                    } else if (spanishVoices.isNotEmpty()) {
                        engine.voice = spanishVoices[0]
                    }
                } catch (_: Exception) {}

                engine.setPitch(1.22f)
                engine.setSpeechRate(0.92f)
                isReady = true
            }
        }
    }

    fun switchToNextVoice(): String {
        val engine = tts ?: return "Sin voces"
        if (spanishVoices.isEmpty()) {
            speak("Solo hay una voz disponible en este teléfono.")
            return "1/1"
        }
        currentVoiceIndex = (currentVoiceIndex + 1) % spanishVoices.size
        val selected = spanishVoices[currentVoiceIndex]
        engine.voice = selected

        appContext.getSharedPreferences("juega_aprende_prefs", Context.MODE_PRIVATE)
            .edit().putString("saved_voice_name", selected.name).apply()

        val label = "${currentVoiceIndex + 1}/${spanishVoices.size}"
        speak("¡Hola! ¿Te gusta esta voz?")
        return label
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
                if (id == utteranceId) onDone?.invoke()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) onDone?.invoke()
            }
        })

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
    }

    fun playSuccessSound() {
        try { toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 220) } catch (_: Exception) {}
    }

    fun playTapSound() {
        try { toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 90) } catch (_: Exception) {}
    }

    fun playWrongSound() {
        try { toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 180) } catch (_: Exception) {}
    }

    fun stop() { tts?.stop() }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        toneGen.release()
    }
}
