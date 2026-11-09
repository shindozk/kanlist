package com.kanlist.data.local.converters

import androidx.room.TypeConverter
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.MediaFormat
import com.kanlist.domain.model.MediaRelationType
import com.kanlist.domain.model.MediaSeason
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaStatusAiring
import com.kanlist.domain.model.MediaTag
import com.kanlist.domain.model.MediaTitle
import com.kanlist.domain.model.MediaType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {

    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // MediaTitle
    @TypeConverter
    fun fromMediaTitle(value: String?): MediaTitle? {
        return value?.let { gson.fromJson(it, MediaTitle::class.java) }
    }

    @TypeConverter
    fun toMediaTitle(title: MediaTitle?): String? {
        return title?.let { gson.toJson(it) }
    }

    // MediaType
    @TypeConverter
    fun fromMediaType(value: String): MediaType {
        return MediaType.valueOf(value)
    }

    @TypeConverter
    fun toMediaType(type: MediaType): String {
        return type.name
    }

    // MediaFormat
    @TypeConverter
    fun fromMediaFormat(value: String): MediaFormat {
        return MediaFormat.valueOf(value)
    }

    @TypeConverter
    fun toMediaFormat(format: MediaFormat): String {
        return format.name
    }

    // MediaStatusAiring
    @TypeConverter
    fun fromMediaStatusAiring(value: String): MediaStatusAiring {
        return MediaStatusAiring.valueOf(value)
    }

    @TypeConverter
    fun toMediaStatusAiring(status: MediaStatusAiring): String {
        return status.name
    }

    // MediaSeason
    @TypeConverter
    fun fromMediaSeason(value: String?): MediaSeason? {
        return value?.let { MediaSeason.valueOf(it) }
    }

    @TypeConverter
    fun toMediaSeason(season: MediaSeason?): String? {
        return season?.name
    }

    // MediaSource
    @TypeConverter
    fun fromMediaSource(value: String): MediaSource {
        return MediaSource.valueOf(value)
    }

    @TypeConverter
    fun toMediaSource(source: MediaSource): String {
        return source.name
    }

    // MediaStatus
    @TypeConverter
    fun fromMediaStatus(value: String): MediaStatus {
        return MediaStatus.valueOf(value)
    }

    @TypeConverter
    fun toMediaStatus(status: MediaStatus): String {
        return status.name
    }

    // MediaRelationType
    @TypeConverter
    fun fromMediaRelationType(value: String): MediaRelationType {
        return MediaRelationType.valueOf(value)
    }

    @TypeConverter
    fun toMediaRelationType(type: MediaRelationType): String {
        return type.name
    }

    // MediaTag list
    @TypeConverter
    fun fromMediaTagList(value: String): List<MediaTag> {
        val type = object : TypeToken<List<MediaTag>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMediaTagList(list: List<MediaTag>): String {
        return gson.toJson(list)
    }

    // String list
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    // MediaStudioEntity list
    @TypeConverter
    fun fromMediaStudioList(value: String): List<com.kanlist.data.local.MediaStudioEntity> {
        val type = object : TypeToken<List<com.kanlist.data.local.MediaStudioEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMediaStudioList(list: List<com.kanlist.data.local.MediaStudioEntity>): String {
        return gson.toJson(list)
    }

    // MediaStaffEntity list
    @TypeConverter
    fun fromMediaStaffList(value: String): List<com.kanlist.data.local.MediaStaffEntity> {
        val type = object : TypeToken<List<com.kanlist.data.local.MediaStaffEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMediaStaffList(list: List<com.kanlist.data.local.MediaStaffEntity>): String {
        return gson.toJson(list)
    }

    // MediaCharacterEntity list
    @TypeConverter
    fun fromMediaCharacterList(value: String): List<com.kanlist.data.local.MediaCharacterEntity> {
        val type = object : TypeToken<List<com.kanlist.data.local.MediaCharacterEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMediaCharacterList(list: List<com.kanlist.data.local.MediaCharacterEntity>): String {
        return gson.toJson(list)
    }

    // MediaRelationEntity list
    @TypeConverter
    fun fromMediaRelationList(value: String): List<com.kanlist.data.local.MediaRelationEntity> {
        val type = object : TypeToken<List<com.kanlist.data.local.MediaRelationEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMediaRelationList(list: List<com.kanlist.data.local.MediaRelationEntity>): String {
        return gson.toJson(list)
    }

    // MediaRecommendationEntity list
    @TypeConverter
    fun fromMediaRecommendationList(value: String): List<com.kanlist.data.local.MediaRecommendationEntity> {
        val type = object : TypeToken<List<com.kanlist.data.local.MediaRecommendationEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMediaRecommendationList(list: List<com.kanlist.data.local.MediaRecommendationEntity>): String {
        return gson.toJson(list)
    }

    // MediaExternalLinkEntity list
    @TypeConverter
    fun fromMediaExternalLinkList(value: String): List<com.kanlist.data.local.MediaExternalLinkEntity> {
        val type = object : TypeToken<List<com.kanlist.data.local.MediaExternalLinkEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMediaExternalLinkList(list: List<com.kanlist.data.local.MediaExternalLinkEntity>): String {
        return gson.toJson(list)
    }

    // MediaCoverImageEntity
    @TypeConverter
    fun fromMediaCoverImage(value: String): com.kanlist.data.local.MediaCoverImageEntity {
        return gson.fromJson(value, com.kanlist.data.local.MediaCoverImageEntity::class.java)
    }

    @TypeConverter
    fun toMediaCoverImage(image: com.kanlist.data.local.MediaCoverImageEntity): String {
        return gson.toJson(image)
    }

    // MediaNameEntity
    @TypeConverter
    fun fromMediaName(value: String): com.kanlist.data.local.MediaNameEntity {
        return gson.fromJson(value, com.kanlist.data.local.MediaNameEntity::class.java)
    }

    @TypeConverter
    fun toMediaName(name: com.kanlist.data.local.MediaNameEntity): String {
        return gson.toJson(name)
    }

    // MediaVoiceActorEntity list
    @TypeConverter
    fun fromMediaVoiceActorList(value: String): List<com.kanlist.data.local.MediaVoiceActorEntity> {
        val type = object : TypeToken<List<com.kanlist.data.local.MediaVoiceActorEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMediaVoiceActorList(list: List<com.kanlist.data.local.MediaVoiceActorEntity>): String {
        return gson.toJson(list)
    }

    // NextAiringEpisodeEntity
    @TypeConverter
    fun fromNextAiringEpisode(value: String?): com.kanlist.data.local.NextAiringEpisodeEntity? {
        return value?.let { gson.fromJson(it, com.kanlist.data.local.NextAiringEpisodeEntity::class.java) }
    }

    @TypeConverter
    fun toNextAiringEpisode(episode: com.kanlist.data.local.NextAiringEpisodeEntity?): String? {
        return episode?.let { gson.toJson(it) }
    }

    // LocalDate
    @TypeConverter
    fun fromLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun toLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    // LocalDateTime
    @TypeConverter
    fun fromLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it / 1000, 0, java.time.ZoneOffset.UTC) }
    }

    @TypeConverter
    fun toLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.toEpochSecond(java.time.ZoneOffset.UTC)?.times(1000)
    }

    // String list for collection mediaIds
    @TypeConverter
    fun fromCollectionMediaIds(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toCollectionMediaIds(list: List<String>): String {
        return gson.toJson(list)
    }

    // Map<MediaStatus, Int>
    @TypeConverter
    fun fromMediaStatusMap(value: String): Map<MediaStatus, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        val map = gson.fromJson<Map<String, Int>>(value, type)
        return map.mapKeys { MediaStatus.valueOf(it.key) }
    }

    @TypeConverter
    fun toMediaStatusMap(map: Map<MediaStatus, Int>): String {
        val stringMap = map.mapKeys { it.key.name }
        return gson.toJson(stringMap)
    }

    // Map<MediaType, Int>
    @TypeConverter
    fun fromMediaTypeMap(value: String): Map<MediaType, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        val map = gson.fromJson<Map<String, Int>>(value, type)
        return map.mapKeys { MediaType.valueOf(it.key) }
    }

    @TypeConverter
    fun toMediaTypeMap(map: Map<MediaType, Int>): String {
        val stringMap = map.mapKeys { it.key.name }
        return gson.toJson(stringMap)
    }

    // ChapterLanguage list
    @TypeConverter
    fun fromChapterLanguageList(value: String): List<ChapterLanguage> {
        val type = object : TypeToken<List<String>>() {}.type
        val codes = gson.fromJson<List<String>>(value, type)
        return codes.mapNotNull { ChapterLanguage.values().firstOrNull { it.code == it } }
    }

    @TypeConverter
    fun toChapterLanguageList(list: List<ChapterLanguage>): String {
        val codes = list.map { it.code }
        return gson.toJson(codes)
    }

    // MediaSource list
    @TypeConverter
    fun fromMediaSourceList(value: String): List<MediaSource> {
        val type = object : TypeToken<List<String>>() {}.type
        val names = gson.fromJson<List<String>>(value, type)
        return names.mapNotNull { MediaSource.values().firstOrNull { it.name == it } }
    }

    @TypeConverter
    fun toMediaSourceList(list: List<MediaSource>): String {
        val names = list.map { it.name }
        return gson.toJson(names)
    }

    // LibrarySortOrder
    @TypeConverter
    fun fromLibrarySortOrder(value: String): com.kanlist.domain.model.LibrarySortOrder {
        return com.kanlist.domain.model.LibrarySortOrder.valueOf(value)
    }

    @TypeConverter
    fun toLibrarySortOrder(order: com.kanlist.domain.model.LibrarySortOrder): String {
        return order.name
    }

    // ThemeMode
    @TypeConverter
    fun fromThemeMode(value: String): com.kanlist.domain.model.ThemeMode {
        return com.kanlist.domain.model.ThemeMode.valueOf(value)
    }

    @TypeConverter
    fun toThemeMode(mode: com.kanlist.domain.model.ThemeMode): String {
        return mode.name
    }

    // TitleLanguage
    @TypeConverter
    fun fromTitleLanguage(value: String): com.kanlist.domain.model.TitleLanguage {
        return com.kanlist.domain.model.TitleLanguage.valueOf(value)
    }

    @TypeConverter
    fun toTitleLanguage(lang: com.kanlist.domain.model.TitleLanguage): String {
        return lang.name
    }
}