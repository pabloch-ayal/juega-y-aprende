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
                val res = engine.setLanguage(Locale("es", "US"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale("es", "ES"))
                }

                try {
                    val voices = engine.voices?.filter { it.locale.language == "es" } ?: emptyList()
                    // Códigos conocidos de voces femeninas de Google TTS en español
                    val femaleCodes = listOf("sfb", "eef", "esd", "tpf", "f00", "female", "mujer", "sabina", "paulina", "monica", "helena", "laura")
                    // Excluir explícitamente voces masculinas
                    val maleCodes = listOf("sfd", "eec", "esf", "tpd", "m00", "male", "hombre", "jorge", "diego", "carlos", "pablo")

                    val chosenVoice = voices.firstOrNull { v ->
                        val n = v.name.lowercase()
                        !v.isNetworkConnectionRequired && femaleCodes.any { n.contains(it) }
                    } ?: voices.firstOrNull { v ->
                        val n = v.name.lowercase()
                        femaleCodes.any { n.contains(it) }
                    } ?: voices.firstOrNull { v ->
                        val n = v.name.lowercase()
                        !v.isNetworkConnectionRequired && maleCodes.none { n.contains(it) }
                    }

                    if (chosenVoice != null) {
                        engine.voice = chosenVoice
                    }
                } catch (_: Exception) {}

                // Tono fino, dulce y melodioso
                engine.setPitch(1.25f)
                engine.setSpeechRate(0.90f)
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
