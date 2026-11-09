package com.kanlist.domain.repository

import com.kanlist.domain.model.Media

interface TranslationRepository {
    suspend fun translateText(
        text: String,
        sourceLang: String = "en",
        targetLang: String = "pt"
    ): Result<String>

    suspend fun translateSynopsis(media: Media): Result<String>

    fun observeTranslationCache(key: String): Flow<String?>
}