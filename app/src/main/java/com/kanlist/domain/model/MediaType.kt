package com.kanlist.domain.model

import kotlinx.serialization.Serializable

enum class MediaType(
    val displayName: String,
    val displayNamePlural: String,
    val iconName: String,
    val defaultDurationMinutes: Int,
    val pastelBackgroundColor: Int,
    val pastelIconColor: Int,
    val pastelBackgroundColorDark: Int,
    val pastelIconColorDark: Int
) {
    ANIME(
        "Anime", "Animes", "ic_anime",
        24, 0xFFFFDCC4, 0xFFE8741A,
        0x2EE8741A, 0xFFE8741A
    ),
    MOVIE(
        "Filme", "Filmes", "ic_movie",
        105, 0xFFFDE3A7, 0xFFB7791F,
        0x2EB7791F, 0xFFB7791F
    ),
    OVA(
        "OVA/Especial", "OVAs/Especiais", "ic_ova",
        24, 0xFFFFD9D2, 0xFFD9534F,
        0x2ED9534F, 0xFFD9534F
    ),
    DONGHUA(
        "Donghua", "Donghuas", "ic_donghua",
        24, 0xFFFBD5EE, 0xFFC2449B,
        0x2EC2449B, 0xFFC2449B
    ),
    MANGA(
        "Mangá", "Mangás", "ic_manga",
        6, 0xFFE3EFCB, 0xFF5E8B23,
        0x2E5E8B23, 0xFF5E8B23
    ),
    MANHWA(
        "Manhwa", "Manhwas", "ic_manhwa",
        5, 0xFFD7E9F7, 0xFF2F7BBF,
        0x2E2F7BBF, 0xFF2F7BBF
    ),
    MANHUA(
        "Manhua", "Manhuas", "ic_manhua",
        5, 0xFFE6DEFA, 0xFF6C4FD0,
        0x2E6C4FD0, 0xFF6C4FD0
    ),
    NOVEL(
        "Novel/Light Novel", "Novels", "ic_novel",
        30, 0xFFF3E6D8, 0xFF8A5A2B,
        0x2E8A5A2B, 0xFF8A5A2B
    ),
    ONE_SHOT(
        "One-shot", "One-shots", "ic_one_shot",
        10, 0xFFDDF1EC, 0xFF2C8C78,
        0x2E2C8C78, 0xFF2C8C78
    ),
    DOUJIN(
        "Doujin", "Doujins", "ic_doujin",
        10, 0xFFF7DDE4, 0xFFB24668,
        0x2EB24668, 0xFFB24668
    );

    companion object {
        fun fromString(value: String): MediaType? {
            return values().find { it.name.equals(value, ignoreCase = true) }
                ?: values().find { it.displayName.equals(value, ignoreCase = true) }
        }

        val watchTypes = listOf(ANIME, MOVIE, OVA, DONGHUA)
        val readTypes = listOf(MANGA, MANHWA, MANHUA, NOVEL, ONE_SHOT, DOUJIN)

        fun isWatchType(type: MediaType): Boolean = type in watchTypes
        fun isReadType(type: MediaType): Boolean = type in readTypes
    }
}

@Serializable
enum class MediaStatus(
    val displayNameWatch: String,
    val displayNameRead: String,
    val order: Int
) {
    CURRENT("Assistindo", "Lendo", 0),
    PLANNING("Quero assistir", "Quero ler", 1),
    COMPLETED("Completo", "Completo", 2),
    PAUSED("Pausado", "Pausado", 3),
    DROPPED("Abandonado", "Abandonado", 4),
    REWATCHING("Reassistindo", "Relendo", 5);

    fun getDisplayName(type: MediaType): String {
        return if (MediaType.isWatchType(type)) displayNameWatch else displayNameRead
    }

    companion object {
        fun fromString(value: String): MediaStatus? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

@Serializable
enum class MediaFormat(
    val displayName: String
) {
    TV("TV"),
    MOVIE("Filme"),
    OVA("OVA"),
    ONA("ONA"),
    SPECIAL("Especial"),
    MUSIC("Música"),
    MANGA("Mangá"),
    NOVEL("Novel"),
    ONE_SHOT("One-shot"),
    DOUJIN("Doujin"),
    MANHWA("Manhwa"),
    MANHUA("Manhua"),
    UNKNOWN("Desconhecido");
}

@Serializable
enum class MediaSource(
    val displayName: String,
    val baseUrl: String
) {
    ANILIST("AniList", "https://graphql.anilist.co"),
    MANGDEX("MangaDex", "https://api.mangadex.org"),
    KITSU("Kitsu", "https://kitsu.io/api/edge"),
    JIKAN("MyAnimeList (Jikan)", "https://api.jikan.moe/v4");

    companion object {
        val allEnabled = values().toList()
    }
}

@Serializable
enum class TitleLanguage(
    val displayName: String,
    val code: String
) {
    ROMAJI("Romaji", "romaji"),
    ENGLISH("Inglês", "english"),
    NATIVE("Nativo", "native");
}

@Serializable
enum class ChapterLanguage(
    val displayName: String,
    val code: String
) {
    PT_BR("Português (BR)", "pt-br"),
    EN("Inglês", "en"),
    ES("Espanhol", "es"),
    FR("Francês", "fr"),
    JP("Japonês", "ja"),
    KR("Coreano", "ko"),
    ZH("Chinês", "zh");
}

@Serializable
enum class MediaRelationType(
    val displayName: String
) {
    SEQUEL("Sequência"),
    PREQUEL("Prequência"),
    PARENT("Obra principal"),
    SIDE_STORY("História paralela"),
    SPIN_OFF("Spin-off"),
    SUMMARY("Resumo"),
    ALTERNATIVE("Versão alternativa"),
    ADAPTATION("Adaptação"),
    CHARACTER("Personagens em comum"),
    OTHER("Outro");
}

@Serializable
enum class MediaSeason(
    val displayName: String,
    val order: Int
) {
    WINTER("Inverno", 0),
    SPRING("Primavera", 1),
    SUMMER("Verão", 2),
    FALL("Outono", 3);
}

@Serializable
enum class MediaStatusAiring(
    val displayName: String
) {
    FINISHED("Finalizado"),
    RELEASING("Em lançamento"),
    NOT_YET_RELEASED("Em breve"),
    HIATUS("Em hiato"),
    CANCELLED("Cancelado"),
    UNKNOWN("Desconhecido");
}