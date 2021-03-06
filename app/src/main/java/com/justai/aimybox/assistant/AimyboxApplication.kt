package com.justai.aimybox.assistant

import android.app.Application
import com.justai.aimybox.Aimybox
import com.justai.aimybox.api.aimybox.AimyboxDialogApi
import com.justai.aimybox.components.AimyboxProvider
import com.justai.aimybox.core.Config
import com.justai.aimybox.speechkit.google.platform.GooglePlatformSpeechToText
import com.justai.aimybox.speechkit.justai.JustAiTextToSpeech
import com.justai.aimybox.speechkit.justai.JustAiTextToSpeech.Config.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.CoroutineContext

class AimyboxApplication : Application(), AimyboxProvider, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    companion object {
        private const val AIMYBOX_API_KEY = "Ldf0j7WZi3KwNah2aNeXVIACz0lb9qMH"
    }

    private val speechToText = GooglePlatformSpeechToText(this, Locale("RU"))

    private val textToSpeech by lazy {
        val user = "user"
        val password = "password"
        val textToSpeechConfig =
            JustAiTextToSpeech.Config(
                voice = Voice.WOMAN_1
            )
        JustAiTextToSpeech(this, user, password, textToSpeechConfig)
    }

    fun changeVoice(voice: Voice) = textToSpeech.changeVoice(voice)

    val aimyboxConfig by lazy {
        val unitId = UUID.randomUUID().toString()

        val dialogApi = AimyboxDialogApi(AIMYBOX_API_KEY, unitId)

        Config.create(speechToText, textToSpeech, dialogApi)
    }

    override val aimybox by lazy { Aimybox(aimyboxConfig) }
}