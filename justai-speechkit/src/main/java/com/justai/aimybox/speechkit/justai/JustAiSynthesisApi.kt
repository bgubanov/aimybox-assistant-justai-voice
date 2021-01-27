package com.justai.aimybox.speechkit.justai

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


internal class JustAiSynthesisApi(
    private val user: String,
    private val password: String
) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    suspend fun request(
        text: String,
        config: JustAiTextToSpeech.Config
    ): ByteArray {
        val requestUrl = config.apiUrl.toHttpUrl().toString()

        val JSON = "application/json; charset=UTF-8".toMediaTypeOrNull()
        val jsonString = getJsonString(text)
        val requestBody = jsonString.toRequestBody(JSON)

        val credential = Credentials.basic(user, password)

        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .addHeader("Authorization", credential)
            .build()

        return suspendCancellableCoroutine<InputStream> { continuation ->
            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    L.e("Exception occurred during API request. Request: $call")
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body
                    if (body == null) {
                        continuation.resumeWithException(NullPointerException("Body is null"))
                    } else {
                        continuation.resume(body.byteStream())
                    }
                }
            })
        }.use(InputStream::readBytes)
    }

    private fun getJsonString(text: String) = "{\"utterance\": \"$text\"}"
}