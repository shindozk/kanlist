package com.kanlist.presentation.ui.workdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Chip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.kanlist.domain.model.Chapter
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.UserMedia
import com.kanlist.presentation.theme.KanlistTheme
import com.kanlist.presentation.theme.MediaTileBackgroundColor
import com.kanlist.presentation.theme.MediaTileIconColor
import com.kanlist.presentation.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDetailScreen(
    mediaId: String,
    viewModel: WorkDetailViewModel = viewModel()
) {
    val mediaResult by viewModel.media.collectAsStateWithLifecycle()
    val userMedia by viewModel.userMedia.collectAsStateWithLifecycle()
    val episodes by viewModel.episodes.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val chapterProgress by viewModel.chapterProgress.collectAsStateWithLifecycle()
    val relations by viewModel.relations.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val staff by viewModel.staff.collectAsStateWithLifecycle()
    val externalLinks by viewModel.externalLinks.collectAsStateWithLifecycle()
    val translatedSynopsis by viewModel.translatedSynopsis.collectAsStateWithLifecycle()
    val showTranslated by viewModel.showTranslated.collectAsStateWithLifecycle()
    val selectedChapterLanguages by viewModel.selectedChapterLanguages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()

    // Load data on first composition
    androidx.compose.runtime.LaunchedEffect(mediaId) {
        viewModel.load(mediaId)
    }

    KanlistTheme {
        val media = mediaResult.getOrNull()
        
        if (media == null) {
            LoadingScreen()
        } else {
            WorkDetailContent(
                media = media,
                userMedia = userMedia,
                episodes = episodes,
                chapters = chapters,
                chapterProgress = chapterProgress,
                relations = relations,
                recommendations = recommendations,
                characters = characters,
                staff = staff,
                externalLinks = externalLinks,
                translatedSynopsis = translatedSynopsis,
                showTranslated = showTranslated,
                selectedChapterLanguages = selectedChapterLanguages,
                collections = collections,
                onIncrementProgress = { viewModel.incrementProgress() },
                onUpdateProgress = { progress, status -> viewModel.updateProgress(progress, status) },
                onUpdateScore = { viewModel.updateScore(it) },
                onUpdateNotes = { viewModel.updateNotes(it) },
                onToggleFavorite = { viewModel.toggleFavorite() },
                onAddToCollection = { viewModel.addToCollection(it) },
                onRemoveFromCollection = { viewModel.removeFromCollection(it) },
                onRemoveFromLibrary = { viewModel.removeFromLibrary() },
                onResetProgress = { viewModel.resetProgress() },
                onMarkChapterRead = { chapterId, number -> viewModel.markChapterRead(chapterId, number) },
                onMarkChapterUnread = { viewModel.markChapterUnread(it) },
                onMarkChaptersUpTo = { viewModel.markChaptersUpTo(it) },
                onToggleTranslation = { viewModel.toggleTranslation() },
                onChapterLanguagesChange = { viewModel.updateChapterLanguages(it) }
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
fun WorkDetailContent(
    media: Media,
    userMedia: UserMedia?,
    episodes: List<com.kanlist.domain.model.Episode>,
    chapters: com.kanlist.domain.model.ChapterList?,
    chapterProgress: Set<Double>,
    relations: List<com.kanlist.domain.model.MediaRelation>,
    recommendations: List<com.kanlist.domain.model.MediaRecommendation>,
    characters: List<com.kanlist.domain.model.MediaCharacter>,
    staff: List<com.kanlist.domain.model.MediaStaff>,
    externalLinks: List<com.kanlist.domain.model.MediaExternalLink>,
    translatedSynopsis: String?,
    showTranslated: Boolean,
    selectedChapterLanguages: List<ChapterLanguage>,
    collections: List<com.kanlist.domain.model.Collection>,
    onIncrementProgress: () -> Unit,
    onUpdateProgress: (Int, MediaStatus?) -> Unit,
    onUpdateScore: (Float) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToCollection: (String) -> Unit,
    onRemoveFromCollection: (String) -> Unit,
    onRemoveFromLibrary: () -> Unit,
    onResetProgress: () -> Unit,
    onMarkChapterRead: (String, Double) -> Unit,
    onMarkChapterUnread: (String) -> Unit,
    onMarkChaptersUpTo: (Double) -> Unit,
    onToggleTranslation: () -> Unit,
    onChapterLanguagesChange: (List<ChapterLanguage>) -> Unit
) {
    val isWatchType = media.type in MediaType.watchTypes
    val progress = userMedia?.progressPercentage ?: 0f
    val currentEp = userMedia?.progress ?: 0
    val totalEp = media.totalEpisodesOrChapters
    val nextEp = currentEp + 1

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        // Banner + Cover header
        item { WorkHeader(media = media, userMedia = userMedia) }

        // Tracking card
        item { TrackingCard(
            media = media,
            userMedia = userMedia,
            progress = progress,
            nextEp = nextEp,
            totalEp = totalEp,
            isWatchType = isWatchType,
            estimatedTime = userMedia?.estimatedTimeRemaining ?: "",
            onIncrementProgress = onIncrementProgress,
            onUpdateProgress = onUpdateProgress,
            onUpdateScore = onUpdateScore,
            onToggleFavorite = onToggleFavorite,
            collections = collections,
            onAddToCollection = onAddToCollection,
            onRemoveFromCollection = onRemoveFromCollection
        )}

        // Synopsis
        item { SynopsisSection(
            media = media,
            translatedSynopsis = translatedSynopsis,
            showTranslated = showTranslated,
            onToggleTranslation = onToggleTranslation
        )}

        // Episodes/Chapters list
        if (isWatchType) {
            item { EpisodesSection(
                episodes = episodes,
                currentEp = currentEp,
                onMarkEpisode = { /* implement */ }
            )}
        } else {
            item { ChaptersSection(
                chapters = chapters?.chapters ?: emptyList(),
                chapterProgress = chapterProgress,
                selectedLanguages = selectedChapterLanguages,
                onLanguageChange = onChapterLanguagesChange,
                onMarkChapterRead = onMarkChapterRead,
                onMarkChapterUnread = onMarkChapterUnread,
                onMarkUpTo = onMarkChaptersUpTo
            )}
        }

        // Info
        item { InfoSection(media = media) }

        // Characters
        if (characters.isNotEmpty()) {
            item { CharactersSection(characters = characters) }
        }

        // Staff
        if (staff.isNotEmpty()) {
            item { StaffSection(staff = staff) }
        }

        // Relations
        if (relations.isNotEmpty()) {
            item { RelationsSection(relations = relations) }
        }

        // Recommendations
        if (recommendations.isNotEmpty()) {
            item { RecommendationsSection(recommendations = recommendations) }
        }

        // External links
        if (externalLinks.isNotEmpty()) {
            item { ExternalLinksSection(links = externalLinks) }
        }

        // Actions menu
        item { ActionsMenu(
            userMedia = userMedia,
            onRemoveFromLibrary = onRemoveFromLibrary,
            onResetProgress = onResetProgress,
            onUpdateNotes = onUpdateNotes
        )}
    }
}

@Composable
fun WorkHeader(media: Media, userMedia: UserMedia?) {
    val tileBg = MediaTileBackgroundColor(media.type)
    val tileIcon = MediaTileIconColor(media.type)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Banner
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(media.bannerImage ?: media.coverImage.extraLarge ?: media.coverImage.large)
                .crossfade(true)
                .build(),
            contentDescription = "",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
        )

        // Cover + Title overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = -60.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cover
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.coverImage.large ?: media.coverImage.medium)
                    .crossfade(true)
                    .transformations(RoundedCornersTransformation(16.dp))
                    .build(),
                contentDescription = media.displayTitle,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.size(90.dp, 135.dp)
            )

            // Title & badges
            Column(
                modifier = Modifier.weight(1f).padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Type tile
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .background(
                                color = tileBg.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = media.type.displayName, style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tileIcon)
                    }

                    // Status badge
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .background(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = media.status.displayName, style = Typography.labelSmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Year
                    media.seasonYear?.let { year ->
                        Text(text = year.toString(), style = Typography.labelMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Text(text = media.displayTitle, style = Typography.headlineMedium, maxLines = 2, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                media.title.english?.let { Text(text = it, style = Typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }

                // Score + episodes/chapters
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    media.averageScore?.let { score ->
                        Text(text = "${String.format("%.1f", score)} ★", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                    }
                    if (media.totalEpisodesOrChapters > 0) {
                        Text(text = "${media.totalEpisodesOrChapters} ${if (media.type in MediaType.watchTypes) "eps" else "caps"}", style = Typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Next episode airing
                media.nextAiringEpisode?.let { nextEp ->
                    val timeLeft = java.time.Duration.ofMillis(nextEp.timeUntilAiring)
                    val days = timeLeft.toDays()
                    val hours = timeLeft.toHoursPart()
                    Text(
                        text = "Próximo ep em ${if (days > 0) "$days dias " else ""}$hours horas",
                        style = Typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun TrackingCard(
    media: Media,
    userMedia: UserMedia?,
    progress: Float,
    nextEp: Int,
    totalEp: Int,
    isWatchType: Boolean,
    estimatedTime: String,
    onIncrementProgress: () -> Unit,
    onUpdateProgress: (Int, MediaStatus?) -> Unit,
    onUpdateScore: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    collections: List<com.kanlist.domain.model.Collection>,
    onAddToCollection: (String) -> Unit,
    onRemoveFromCollection: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress ring + stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress ring
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    ProgressRingLarge(progress = progress)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${progress.roundToInt()}%", style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = "$nextEp / $totalEp", style = Typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status selector
                    StatusSelector(
                        currentStatus = userMedia?.status ?: MediaStatus.PLANNING,
                        mediaType = media.type,
                        onStatusChange = { status -> onUpdateProgress(userMedia?.progress ?: 0, status) }
                    )

                    // Time remaining
                    Text(text = "$estimatedTime restantes", style = Typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)

                    // Progress bar
                    LinearProgressIndicator(
                        progress = progress / 100f,
                        trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Big action button
            Button(
                onClick = onIncrementProgress,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = if (isWatchType) "Marcar ep $nextEp" else "Marcar cap $nextEp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Score + Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Sua nota", style = Typography.labelSmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { onUpdateScore(star.toFloat()) }) {
                                Icon(
                                    imageVector = if ((userMedia?.score ?: 0f) >= star) 
                                        androidx.compose.material.icons.filled.Star 
                                    else 
                                        androidx.compose.material.icons.outlined.Star,
                                    contentDescription = "Estrela $star",
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    size = 24.dp
                                )
                            }
                        }
                    }
                }

                // Favorite
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Favorito", style = Typography.labelSmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (userMedia?.isFavorite == true) 
                                androidx.compose.material.icons.filled.Favorite 
                            else 
                                androidx.compose.material.icons.outlined.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (userMedia?.isFavorite == true) androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 28.dp
                        )
                    }
                }

                // Collections
                if (collections.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Coleções", style = Typography.labelSmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        androidx.compose.material3.MenuAnchor { anchor ->
                            IconButton(onClick = { anchor.open() }) {
                                Icon(imageVector = androidx.compose.material.icons.outlined.Folder, contentDescription = "Coleções", tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } popover = {
                            androidx.compose.material3.DropdownMenu(expanded = true, onDismissRequest = {}) {
                                collections.forEach { collection ->
                                    val isInCollection = userMedia?.collectionIds.contains(collection.id) == true
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (isInCollection) {
                                                    Icon(imageVector = androidx.compose.material.icons.filled.Check, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, size = 20.dp)
                                                }
                                                Text(text = collection.name)
                                            }
                                        },
                                        onClick = { if (isInCollection) onRemoveFromCollection(collection.id) else onAddToCollection(collection.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressRingLarge(progress: Float) {
    Canvas(modifier = Modifier.size(80.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.width - 8.dp.value) / 2f
        
        drawCircle(color = MaterialTheme.colorScheme.surfaceVariant, center = center, radius = radius, style = Stroke(8.dp.value))
        val sweepAngle = 360 * (progress / 100)
        drawArc(
            color = MaterialTheme.colorScheme.primary,
            center = center,
            radius = radius,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(8.dp.value, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StatusSelector(
    currentStatus: MediaStatus,
    mediaType: MediaType,
    onStatusChange: (MediaStatus) -> Unit
) {
    androidx.compose.material3.MenuAnchor { anchor ->
        Button(
            onClick = { anchor.open() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = currentStatus.getDisplayName(mediaType), fontWeight = FontWeight.Medium)
                Icon(imageVector = androidx.compose.material.icons.outlined.ExpandMore, contentDescription = "")
            }
        }
    } popover = {
        DropdownMenu(expanded = true, onDismissRequest = {}) {
            MediaStatus.values().forEach { status ->
                DropdownMenuItem(
                    text = { Text(text = status.getDisplayName(mediaType)) },
                    onClick = { onStatusChange(status) }
                )
            }
        }
    }
}

@Composable
fun SynopsisSection(
    media: Media,
    translatedSynopsis: String?,
    showTranslated: Boolean,
    onToggleTranslation: () -> Unit
) {
    val synopsis = media.description ?: return
    val cleanSynopsis = synopsis
        .replace("<[^>]*>".toRegex(), "")
        .replace("&nbsp;".toRegex(), " ")
        .replace("&".toRegex(), "&")
        .replace("<".toRegex(), "<")
        .replace(">".toRegex(), ">")
        .trim()

    val displayText = if (showTranslated && translatedSynopsis != null) translatedSynopsis else cleanSynopsis
    val isTranslated = showTranslated && translatedSynopsis != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sinopse", style = Typography.titleMedium)
                if (translatedSynopsis != null) {
                    Button(onClick = onToggleTranslation) {
                        Text(text = if (isTranslated) "Ver original (EN)" else "Ver tradução (PT)")
                    }
                }
            }

            Text(text = displayText, style = Typography.bodyMedium, maxLines = if (showTranslated) Int.MAX_VALUE else 5, overflow = TextOverflow.Ellipsis)

            if (!showTranslated && cleanSynopsis.length > 300) {
                TextButton(onClick = { /* expand */ }) {
                    Text(text = "Ler mais")
                }
            }
        }
    }
}

@Composable
fun EpisodesSection(episodes: List<com.kanlist.domain.model.Episode>, currentEp: Int, onMarkEpisode: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Episódios (${episodes.size})", style = Typography.titleMedium)
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(episodes) { episode ->
                    EpisodeRow(
                        episode = episode,
                        isWatched = episode.number <= currentEp,
                        onClick = { onMarkEpisode(episode.number) }
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodeRow(episode: com.kanlist.domain.model.Episode, isWatched: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                color = if (isWatched) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isWatched) 
                    androidx.compose.material.icons.filled.CheckCircle 
                else 
                    androidx.compose.material.icons.outlined.RadioButtonUnchecked,
                contentDescription = "",
                tint = if (isWatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 24.dp
            )
        }

        // Episode info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "Episódio ${episode.number}", style = Typography.bodyMedium)
            episode.title?.let { Text(text = it, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            episode.airDate?.let { Text(text = "Exibido em ${it.formatDate()}", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun ChaptersSection(
    chapters: List<Chapter>,
    chapterProgress: Set<Double>,
    selectedLanguages: List<ChapterLanguage>,
    onLanguageChange: (List<ChapterLanguage>) -> Unit,
    onMarkChapterRead: (String, Double) -> Unit,
    onMarkChapterUnread: (String) -> Unit,
    onMarkUpTo: (Double) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Capítulos (${chapters.size})", style = Typography.titleMedium)
                
                // Language filter
                androidx.compose.material3.MenuAnchor { anchor ->
                    Button(onClick = { anchor.open() }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = androidx.compose.material.icons.outlined.Language, contentDescription = "")
                            Text(text = selectedLanguages.joinToString(", ") { it.displayName })
                        }
                    }
                } popover = {
                    DropdownMenu(expanded = true, onDismissRequest = {}) {
                        ChapterLanguage.values().forEach { lang ->
                            val isSelected = selectedLanguages.contains(lang)
                            DropdownMenuItem(
                                text = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (isSelected) Icon(imageVector = androidx.compose.material.icons.filled.Check, contentDescription = "", size = 20.dp)
                                        Text(text = lang.displayName)
                                    }
                                },
                                onClick = { 
                                    val newLangs = if (isSelected) selectedLanguages - lang else selectedLanguages + lang
                                    onLanguageChange(newLangs)
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(chapters) { chapter ->
                    val isRead = chapterProgress.contains(chapter.chapterNumber)
                    ChapterRow(
                        chapter = chapter,
                        isRead = isRead,
                        onClick = { if (isRead) onMarkChapterUnread(chapter.id) else onMarkChapterRead(chapter.id, chapter.chapterNumber) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChapterRow(chapter: Chapter, isRead: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                color = if (isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isRead) androidx.compose.material.icons.filled.CheckCircle else androidx.compose.material.icons.outlined.RadioButtonUnchecked,
                contentDescription = "",
                tint = if (isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 24.dp
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Cap. ${chapter.chapter}", style = Typography.bodyMedium)
                chapter.volume?.let { Text(text = "Vol. $it", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                chapter.groupName?.let { Text(text = "[$it]", style = Typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
            }
            chapter.title?.let { Text(text = it, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = chapter.language.uppercase(), style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                chapter.publishAt?.let { Text(text = "Lançado em ${it.formatDate()}", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
fun InfoSection(media: Media) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Informações", style = Typography.titleMedium)
            
            InfoRow(label = "Formato", value = media.format.displayName)
            media.season?.let { InfoRow(label = "Temporada", value = "${it.displayName} ${media.seasonYear ?: ""}") }
            InfoRow(label = "Início", value = media.startDate?.formatDate() ?: "Desconhecido")
            media.endDate?.let { InfoRow(label = "Fim", value = it.formatDate()) }
            media.duration?.let { InfoRow(label = "Duração", value = "${it} min por ${if (media.type in MediaType.watchTypes) "ep" else "cap"}") }
            media.studios.filter { it.isMain }.firstOrNull()?.let { InfoRow(label = "Estúdio/Autor", value = it.name) }
            media.genres.take(3).let { if (it.isNotEmpty()) InfoRow(label = "Gêneros", value = it.joinToString(", ")) }
            InfoRow(label = "País", value = media.countryOfOrigin ?: "Desconhecido")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = Typography.bodyMedium, textAlign = TextAlign.End)
    }
}

@Composable
fun CharactersSection(characters: List<com.kanlist.domain.model.MediaCharacter>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Personagens (${characters.size})", style = Typography.titleMedium)
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(characters.take(10)) { character ->
                    CharacterCard(character = character)
                }
            }
        }
    }
}

@Composable
fun CharacterCard(character: com.kanlist.domain.model.MediaCharacter) {
    Column(modifier = Modifier.size(80.dp, 120.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(80.dp, 80.dp).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            // Character image placeholder
            Icon(imageVector = androidx.compose.material.icons.outlined.Person, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 32.dp)
        }
        Text(text = character.name.full ?: "Desconhecido", style = Typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        character.role?.let { Text(text = it, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center) }
    }
}

@Composable
fun StaffSection(staff: List<com.kanlist.domain.model.MediaStaff>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Equipe", style = Typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(staff.take(10)) { member ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = member.role, style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(text = member.name.full ?: "Desconhecido", style = Typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun RelationsSection(relations: List<com.kanlist.domain.model.MediaRelation>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Relacionados", style = Typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(relations) { relation ->
                    RelationRow(relation = relation)
                }
            }
        }
    }
}

@Composable
fun RelationRow(relation: com.kanlist.domain.model.MediaRelation) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = relation.relationType.displayName, style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = relation.media.displayTitle, style = Typography.bodyMedium)
            Text(text = relation.media.type.displayName, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RecommendationsSection(recommendations: List<com.kanlist.domain.model.MediaRecommendation>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Se você gostou...", style = Typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recommendations.take(10)) { rec ->
                    RecommendationCard(recommendation = rec)
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: com.kanlist.domain.model.MediaRecommendation) {
    Card(modifier = Modifier.size(100.dp, 160.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(100.dp, 100.dp).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = androidx.compose.material.icons.outlined.Image, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 32.dp)
            }
            Text(text = recommendation.media.displayTitle, style = Typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            recommendation.rating?.let { Text(text = "$it%", style = Typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun ExternalLinksSection(links: List<com.kanlist.domain.model.MediaExternalLink>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Onde encontrar", style = Typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(links) { link ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = androidx.compose.material.icons.outlined.OpenInNew, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        Text(text = link.site, style = Typography.bodyMedium)
                        Spacer(Modifier.weight(1f))
                        if (link.url != null) {
                            // Would open URL
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionsMenu(
    userMedia: UserMedia?,
    onRemoveFromLibrary: () -> Unit,
    onResetProgress: () -> Unit,
    onUpdateNotes: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Opções", style = Typography.titleMedium)
            
            if (userMedia != null) {
                MenuItem(title = "Zerar progresso", icon = androidx.compose.material.icons.outlined.Refresh, onClick = onResetProgress, isDestructive = true)
                MenuItem(title = "Remover da biblioteca", icon = androidx.compose.material.icons.outlined.Delete, onClick = onRemoveFromLibrary, isDestructive = true)
            }
        }
    }
}

@Composable
fun MenuItem(title: String, icon: @Composable () -> Unit, onClick: () -> Unit, isDestructive: Boolean = false) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = TextButtonDefaults.textButtonColors(
            contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = icon, contentDescription = "", tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = title, color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
        }
    }
}