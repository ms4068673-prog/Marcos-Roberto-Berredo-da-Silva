package com.example

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Data classes for request/response
@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String)

@Serializable
data class GenerateContentResponse(val candidates: List<Candidate>)

@Serializable
data class Candidate(val content: Content)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }
}

object GeminiRepository {
    suspend fun generateMaterial(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part("Você é um especialista em educação adaptada. Seu objetivo é gerar materiais didáticos (resumo e 5 exercícios) seguindo a BNCC, compatíveis com MS Word (use HTML simples para formatação) e contendo descrições detalhadas de ilustrações entre colchetes.")))
        )
        try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Erro na geração."
        } catch (e: Exception) {
            "Erro: ${e.message}"
        }
    }
}
