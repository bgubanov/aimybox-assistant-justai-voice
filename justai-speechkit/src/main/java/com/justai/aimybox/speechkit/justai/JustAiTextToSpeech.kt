package com.justai.aimybox.speechkit.justai

import android.content.Context
import com.justai.aimybox.model.AudioSpeech
import com.justai.aimybox.model.TextSpeech
import com.justai.aimybox.texttospeech.BaseTextToSpeech
import kotlinx.coroutines.cancel

class JustAiTextToSpeech(
    context: Context,
    user: String,
    password: String,
    private val config: Config
) : BaseTextToSpeech(context) {

    private val api = JustAiSynthesisApi(user, password)

    override suspend fun speak(speech: TextSpeech) {
        try {
            val audioData = api.request(speech.text, config)
            onEvent(Event.SpeechDataReceived(audioData))
            audioSynthesizer.play(AudioSpeech.Bytes(audioData))
        } catch (e: Throwable) {
            throw JustAiTextToSpeechException(cause = e)
        }
    }

    override fun destroy() {
        super.destroy()
        coroutineContext.cancel()
    }

    fun changeVoice(voice: Config.Voice) {
        config.voice = voice
    }

    data class Config(
        var voice: Voice = Voice.WOMAN_1
    ) {
        val apiUrl: String
            get() = "http://95.216.71.118:${voice.code}/speak/"

        enum class Voice(val code: String) {
            WOMAN_1("4224"),
            MAN_1("4226"),
            GERALT("4227")
        }
    }
}

