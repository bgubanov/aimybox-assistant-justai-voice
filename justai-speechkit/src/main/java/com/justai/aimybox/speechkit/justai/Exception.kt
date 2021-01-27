package com.justai.aimybox.speechkit.justai

import com.justai.aimybox.core.SpeechToTextException
import com.justai.aimybox.core.TextToSpeechException

class JustAiTextToSpeechException(
    message: String? = null,
    cause: Throwable? = null
) : TextToSpeechException(message, cause)