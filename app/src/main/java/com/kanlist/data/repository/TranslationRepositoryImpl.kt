package com.kanlist.data.repository

import com.kanlist.data.local.KanlistDatabase
import com.kanlist.data.local.Daos
import com.kanlist.data.source.api.ApiService
import com.kanlist.domain.model.Media
import com.kanlist.domain.repository.TranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val database: KanlistDatabase,
    private val translationApi: ApiService.TranslationApiService
) : TranslationRepository {

    private val cacheDao = database.cacheDao()
    private val translationCache = MutableStateFlow<Map<String, String>>(emptyMap())

    override suspend fun translateText(
        text: String,
        sourceLang: String = "en",
        targetLang: String = "pt"
    ): Result<String> = withContext(Dispatchers.IO) {
        val cacheKey = "translate_${sourceLang}_${targetLang}_${text.hashCode()}"
        
        // Check cache
        val cached = cacheDao.get(cacheKey, LocalDateTime.now())
        cached?.let { return@withContext Result.success(it) }

        try {
            val langPair = "$sourceLang|$targetLang"
            val response = translationApi.translate(text, langPair)
            
            if (response.isSuccessful && response.body()?.responseData != null) {
                val translated = response.body()!!.responseData.translatedText
                // Cache for 30 days
                val expiresAt = LocalDateTime.now().plusDays(30)
                cacheDao.insert(com.kanlist.data.local.EntityModels.CacheEntryEntity(
                    key = cacheKey,
                    value = translated,
                    expiresAt = expiresAt,
                    source = "mymemory"
                ))
                return@withContext Result.success(translated)
            }
            
            Result.failure(Exception("Translation failed: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun translateSynopsis(media: Media): Result<String> = withContext(Dispatchers.IO) {
        val description = media.description ?: return@withContext Result.failure(Exception("No description to translate"))
        
        // Check if already in Portuguese (simple heuristic)
        if (isLikelyPortuguese(description)) {
            return@withContext Result.success(description)
        }

        // Clean HTML tags
        val cleanText = description
            .replace("<[^>]*>".toRegex(), "")
            .replace("&nbsp;".toRegex(), " ")
            .replace("&".toRegex(), "&")
            .replace("<".toRegex(), "<")
            .replace(">".toRegex(), ">")
            .replace(""".toRegex(), "\"")
            .trim()

        if (cleanText.length > 5000) {
            // Translate in chunks
            return@withContext translateLongText(cleanText)
        }

        translateText(cleanText, "en", "pt")
    }

    private suspend fun translateLongText(text: String): Result<String> {
        val chunks = text.chunked(4000) { it.lastIndexOf('.') + 1 }.filter { it.isNotBlank() }
        val translatedChunks = mutableListOf<String>()
        
        for (chunk in chunks) {
            val result = translateText(chunk, "en", "pt")
            result.onSuccess { translatedChunks.add(it) }
                .onFailure { return@translateLongText Result.failure(it) }
        }
        
        Result.success(translatedChunks.joinToString(" "))
    }

    private fun isLikelyPortuguese(text: String): Boolean {
        val ptWords = setOf("o", "a", "os", "as", "um", "uma", "e", "de", "do", "da", "dos", "das", "em", "no", "na", "nos", "nas", "para", "com", "que", "se", "não", "mais", "como", "mas", "ou", "seu", "sua", "seus", "suas", "meu", "minha", "nosso", "nossa", "este", "esta", "isto", "aquele", "aquela", "aquilo")
        val words = text.toLowerCase().split("\\W+".toRegex()).filter { it.length > 2 }.take(50)
        val ptCount = words.count { ptWords.contains(it) }
        return ptCount.toDouble() / words.size > 0.3
    }

    override fun observeTranslationCache(key: String): Flow<String?> {
        return translationCache.map { it[key] }.distinctUntilChanged()
    }
}