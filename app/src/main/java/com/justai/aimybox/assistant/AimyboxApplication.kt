package com.justai.aimybox.assistant

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.justai.aimybox.Aimybox
import com.justai.aimybox.api.aimybox.AimyboxDialogApi
//import com.justai.aimybox.assistant.pocketsphinx.PocketSphinxMultipleVoiceTrigger
import com.justai.aimybox.assistant.skills.*
import com.justai.aimybox.components.AimyboxProvider
import com.justai.aimybox.core.Config
import com.justai.aimybox.speechkit.google.platform.GooglePlatformSpeechToText
import com.justai.aimybox.speechkit.kaldi.KaldiAssets
import com.justai.aimybox.speechkit.kaldi.KaldiVoiceTrigger
//import com.justai.aimybox.speechkit.pocketsphinx.PocketsphinxAssets
//import com.justai.aimybox.speechkit.pocketsphinx.PocketsphinxRecognizerProvider
import com.justai.aimybox.speechkit.yandex.cloud.*
import com.justai.aimybox.voicetrigger.VoiceTrigger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.*
import kotlin.coroutines.CoroutineContext

class AimyboxApplication : Application(), AimyboxProvider, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    companion object {
        private const val AIMYBOX_API_KEY = ""
        private const val AIMYBOX_WEBHOOK_URL_1 =
            "https://bot.jaicp.com/chatapi/webhook/zenbox/yjdbDjUG:aa7d5a0e4db54501edbf9487493009b287ca9688"
        private const val AIMYBOX_WEBHOOK_URL_2 =
            "https://bot.jaicp.com/chatapi/webhook/zenbox/ebOLoMoe:0ca9b4445627a0bc8ea1f74ceee5c9286332df31"
    }

    private val setOfSkills by lazy {
        linkedSetOf(
            AlarmSkill(this), ReminderSkill(this),
            SettingsSkill(this), TimerSkill(this),
            ChangeBotSkill(this)
        )
    }

    private val speechToText = GooglePlatformSpeechToText(this, Locale("RU"))

    private val marusyaTextToSpeech by lazy {
        val token = "AgAAAAAjWu2CAATuwWlt16g0F0IYrunICaVEoUs"
        val folderId = "b1gvt2nubho67sa74uqh"
        val tokenGenerator = IAmTokenGenerator(token)
        val textToSpeechConfig =
            YandexTextToSpeech.Config(
                voice = Voice.ALENA,
                emotion = Emotion.GOOD,
                speed = Speed.DEFAULT
            )
        YandexTextToSpeech(this, tokenGenerator, folderId, Language.RU, textToSpeechConfig)
    }

    private val solarTextToSpeech by lazy {
        val token = "AgAAAAAjWu2CAATuwWlt16g0F0IYrunICaVEoUs"
        val folderId = "b1gvt2nubho67sa74uqh"
        val tokenGenerator = IAmTokenGenerator(token)
        val textToSpeechConfig =
            YandexTextToSpeech.Config(
                voice = Voice.ZAHAR,
                emotion = Emotion.GOOD,
                speed = Speed.DEFAULT
            )
        YandexTextToSpeech(this, tokenGenerator, folderId, Language.RU, textToSpeechConfig)
    }

//    private val pocketSphinxVoiceTrigger by lazy {
//        val folder = "pocketsphinx/ru"
//        val assets = PocketsphinxAssets
//            .fromApkAssets(
//                this,
//                acousticModelFileName = folder,
//                dictionaryFileName = "${folder}/dictionary.dict",
//                grammarFileName = "${folder}/grammar.gram"
//            )
//
//        val provider = PocketsphinxRecognizerProvider(assets, keywordThreshold = 1e-45f)
//        PocketSphinxMultipleVoiceTrigger(
//            provider, listOf(
//                "абонент" to "1e-30",
//                "привет" to "1e-20"
//            )
//        )
//    }

    private val listTriggers = listOf("маруся",
        "солар", "соло", "сама", "самар", "салат", "с авар", "со лар")

    private val kaldiVoiceTrigger by lazy {
        val kaldiAssets = KaldiAssets.fromApkAssets(this, "kaldi/ru")
        KaldiVoiceTrigger(kaldiAssets, listTriggers)
    }

    private val currentVoiceTrigger by lazy { kaldiVoiceTrigger }

    val firstAimyboxConfig by lazy {
        val unitId = UUID.randomUUID().toString()

        val dialogApi = AimyboxDialogApi(
            AIMYBOX_API_KEY, unitId, AIMYBOX_WEBHOOK_URL_1, setOfSkills
        )

        Config.create(speechToText, marusyaTextToSpeech, dialogApi) {
            this.voiceTrigger = currentVoiceTrigger
        }
    }

    val secondAimyboxConfig by lazy {
        val unitId = UUID.randomUUID().toString()

        val dialogApi = AimyboxDialogApi(
            AIMYBOX_API_KEY, unitId, AIMYBOX_WEBHOOK_URL_2, setOfSkills
        )

        Config.create(speechToText, solarTextToSpeech, dialogApi) {
            voiceTrigger = currentVoiceTrigger
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    fun listenTriggers() {
        launch {
            aimybox.voiceTriggerEvents.asFlow()
                .distinctUntilChanged().collect {
                try {
                    if (it is VoiceTrigger.Event.Triggered) {
                        Log.e("ChangeTrigger", "Triggered: \"${it.phrase}\", ${it.hashCode()}")
                        if (it.phrase?.trim() == listTriggers.first()) {
                            if (aimybox.config !== firstAimyboxConfig)
                                aimybox.updateConfiguration(firstAimyboxConfig)
                        }
                        else {
                            if (aimybox.config !== secondAimyboxConfig)
                                aimybox.updateConfiguration(secondAimyboxConfig)
                        }
                    }
                } catch (t: Throwable) {
                    Log.e("listenTrigger Exception", t.stackTrace.toString())
                    aimybox.standby().join()
                }
            }
        }
    }

    override val aimybox by lazy { Aimybox(firstAimyboxConfig) }
}