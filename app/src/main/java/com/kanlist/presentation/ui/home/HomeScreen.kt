package com.kanlist.presentation.ui.home

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaType
import com.kanlist.presentation.navigation.Screen
import com.kanlist.presentation.theme.KanlistTheme
import com.kanlist.presentation.theme.MediaTileBackgroundColor
import com.kanlist.presentation.theme.MediaTileIconColor
import com.kanlist.presentation.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToWorkDetail: (String) -> Unit,
    onNavigateToWebView: (String, String) -> Unit
) {
    val trendingAnime by viewModel.trendingAnime.collectAsStateWithLifecycle()
    val trendingManga by viewModel.trendingManga.collectAsStateWithLifecycle()
    val seasonalAnime by viewModel.seasonalAnime.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val todayActivity by viewModel.todayActivity.collectAsStateWithLifecycle()
    val weekActivity by viewModel.weekActivity.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val greeting = viewModel.greeting

    KanlistTheme {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Greeting header
                GreetingHeader(greeting = greeting, preferences = preferences)

                // Week strip
                WeekStrip(weekActivity = weekActivity)

                // Continue watching hero card
                continueWatching?.let { media ->
                    ContinueWatchingCard(
                        media = media,
                        userMedia = getUserMediaForMedia(media, viewModel),
                        onClick = { onNavigateToWorkDetail(media.id) },
                        onIncrementProgress = { viewModel.incrementDailyGoal() }
                    )
                }

                // Daily goal
                DailyGoalCard(
                    goal = dailyGoal,
                    streak = stats.currentStreak,
                    onIncrement = { viewModel.incrementDailyGoal() }
                )

                // Today's routine
                TodaysRoutineSection(
                    activities = todayActivity,
                    onIncrementProgress = { viewModel.incrementDailyGoal() }
                )

                // Today's releases (placeholder)
                TodaysReleasesSection()

                // Trending sections
                MediaSection(
                    title = "Em alta - Animes",
                    media = trendingAnime.getOrNull()?.media ?: emptyList(),
                    type = MediaType.ANIME,
                    onItemClick = onNavigateToWorkDetail,
                    seeAllAction = { /* navigate to explore */ }
                )

                MediaSection(
                    title = "Temporada atual",
                    media = seasonalAnime.getOrNull()?.media ?: emptyList(),
                    type = MediaType.ANIME,
                    onItemClick = onNavigateToWorkDetail,
                    seeAllAction = { /* navigate to explore */ }
                )

                MediaSection(
                    title = "Em alta - Mangás",
                    media = trendingManga.getOrNull()?.media ?: emptyList(),
                    type = MediaType.MANGA,
                    onItemClick = onNavigateToWorkDetail,
                    seeAllAction = { /* navigate to explore */ }
                )
            }
        }
    }
}

@Composable
fun GreetingHeader(greeting: String, preferences: com.kanlist.domain.model.UserPreferences) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            androidx.compose.material3.Text(
                text = greeting,
                style = Typography.displayLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
            )
            androidx.compose.material3.Text(
                text = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(java.util.Date()),
                style = Typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Avatar placeholder
        androidx.compose.material3.IconButton(onClick = { /* navigate to profile */ }) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material.icons.outlined.AccountCircle(
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    size = 40.dp
                )
            }
        }
    }
}

@Composable
fun WeekStrip(weekActivity: Map<java.time.LocalDate, List<com.kanlist.domain.model.ActivityEntry>>) {
    val today = java.time.LocalDate.now()
    val startOfWeek = today.minusDays(today.dayOfWeek.value - 1)
    val days = (0..6).map { startOfWeek.plusDays(it) }
    val shortDayNames = arrayOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Day labels
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEachIndexed { index, day ->
                val isToday = day == today
                val hasActivity = weekActivity[day]?.isNotEmpty() == true
                
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = shortDayNames[index],
                        style = androidx.compose.material3.Typography.labelMedium,
                        color = if (isToday) androidx.compose.material3.MaterialTheme.colorScheme.primary
                        else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = day.dayOfMonth.toString(),
                            style = if (isToday) Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            else Typography.titleMedium,
                            color = if (isToday) androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                            else androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        .let { 
                            if (isTrue) {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            color = if (isToday) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                                            else androidx.compose.material3.MaterialTheme.colorScheme.surface,
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) { it }
                            } else { it }
                        }
                    }
                    
                    if (hasActivity) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.size(6.dp)
                            .background(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp)
                            )
                        )
                    } else {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    media: Media,
    userMedia: com.kanlist.domain.model.UserMedia?,
    onClick: () -> Unit,
    onIncrementProgress: () -> Unit
) {
    val progress = userMedia?.progressPercentage ?: 0f
    val currentEp = userMedia?.progress ?: 0
    val totalEp = media.totalEpisodesOrChapters
    val nextEp = currentEp + 1
    val isWatchType = media.type in MediaType.watchTypes

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(200.dp),
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(media.coverImage.large ?: media.coverImage.medium)
                        .crossfade(true)
                        .transformations(RoundedCornersTransformation(12.dp))
                        .build(),
                    contentDescription = media.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp, 120.dp)
                )

                // Info
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "Continue de onde parou",
                        style = Typography.labelMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.Text(
                        text = media.displayTitle,
                        style = Typography.headlineMedium,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                    
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Progress ring
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ProgressRing(progress = progress, strokeWidth = 6.dp)
                            androidx.compose.material3.Text(
                                text = "${progress.roundToInt()}%",
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        androidx.compose.foundation.layout.Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            androidx.compose.material3.Text(
                                text = if (isWatchType) "Ep $nextEp de $totalEp" else "Cap $nextEp de $totalEp",
                                style = Typography.bodyMedium
                            )
                            androidx.compose.material3.Text(
                                text = userMedia?.estimatedTimeRemaining ?: "",
                                style = Typography.bodySmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Continue button
                    androidx.compose.material3.Button(
                        onClick = onIncrementProgress,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        androidx.compose.material3.Text(
                            text = if (isWatchType) "Marcar ep $nextEp" else "Marcar cap $nextEp",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressRing(progress: Float, strokeWidth: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(48.dp)) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = (size.width - strokeWidth.value) / 2f
        
        // Background circle
        drawCircle(
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            center = center,
            radius = radius,
            style = androidx.compose.ui.draw.Stroke(strokeWidth.value)
        )
        
        // Progress arc
        val sweepAngle = 360 * (progress / 100)
        drawArc(
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            center = center,
            radius = radius,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = androidx.compose.ui.draw.Stroke(strokeWidth.value, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun DailyGoalCard(
    goal: com.kanlist.domain.model.DailyGoal,
    streak: Int,
    onIncrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text(
                        text = "Meta de hoje",
                        style = Typography.titleMedium
                    )
                    androidx.compose.material3.Text(
                        text = "${goal.completed}/${goal.target} ${if (goal.target == 1) "episódio/capítulo" else "episódios/capítulos"}",
                        style = Typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "Sequência: $streak dias",
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                    if (goal.isCompleted) {
                        androidx.compose.material3.Text(
                            text = "Meta batida! 🎉",
                            style = Typography.labelSmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Progress bar
            androidx.compose.material3.LinearProgressIndicator(
                progress = goal.progressPercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )

            // Increment button
            androidx.compose.material3.Button(
                onClick = onIncrement,
                enabled = !goal.isCompleted,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                androidx.compose.material3.Text(
                    text = if (goal.isCompleted) "Meta completa!" else "+1 Episódio/Capítulo",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TodaysRoutineSection(
    activities: List<com.kanlist.domain.model.ActivityEntry>,
    onIncrementProgress: () -> Unit
) {
    if (activities.isEmpty()) return

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(text = "Rotina de hoje", style = Typography.titleMedium)
            androidx.compose.material3.Text(
                text = "Ver tudo",
                style = Typography.labelMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }

        androidx.compose.foundation.layout.LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activities) { activity ->
                RoutineRow(activity = activity, onIncrement = onIncrementProgress)
            }
        }
    }
}

@Composable
fun RoutineRow(
    activity: com.kanlist.domain.model.ActivityEntry,
    onIncrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media type tile
            activity.mediaId.let { mediaId ->
                // Would need to get media type from activity
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material.icons.outlined.PlayCircle(
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        size = 28.dp
                    )
                }
            }

            // Info
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.Text(text = "Título da obra", style = Typography.titleSmall)
                androidx.compose.material3.Text(
                    text = "Ep ${activity.progress} • ${activity.timestamp.formatTime()}",
                    style = Typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Check button
            androidx.compose.material3.IconButton(onClick = onIncrement) {
                androidx.compose.material.icons.filled.Check(
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TodaysReleasesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(text = "Lançamentos de hoje", style = Typography.titleMedium)
                androidx.compose.material.icons.outlined.Notifications(
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
            androidx.compose.material3.Text(
                text = "Nenhum lançamento novo hoje",
                style = Typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MediaSection(
    title: String,
    media: List<Media>,
    type: MediaType,
    onItemClick: (String) -> Unit,
    seeAllAction: () -> Unit
) {
    if (media.isEmpty()) return

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(text = title, style = Typography.titleMedium)
            androidx.compose.material3.TextButton(onClick = seeAllAction) {
                androidx.compose.material3.Text(text = "Ver tudo", style = Typography.labelMedium)
            }
        }

        androidx.compose.foundation.layout.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            items(media.take(10)) { item ->
                MediaCard(
                    media = item,
                    onClick = { onItemClick(item.id) }
                )
            }
        }
    }
}

@Composable
fun MediaCard(media: Media, onClick: () -> Unit) {
    val tileBg = MediaTileBackgroundColor(media.type)
    val tileIcon = MediaTileIconColor(media.type)

    Card(
        onClick = onClick,
        modifier = Modifier.size(120.dp, 200.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cover
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.coverImage.large ?: media.coverImage.medium)
                    .crossfade(true)
                    .transformations(RoundedCornersTransformation(22.dp, 22.dp, 0.dp, 0.dp))
                    .build(),
                contentDescription = media.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            // Info
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(horizontal = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Type tile
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .background(
                                color = tileBg.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = media.type.displayName,
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tileIcon
                        )
                    }

                    // Status badge
                    media.status.let { status ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .background(
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = status.displayName,
                                style = Typography.labelSmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                androidx.compose.material3.Text(
                    text = media.displayTitle,
                    style = Typography.titleSmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                )

                androidx.compose.material3.Text(
                    text = "${media.averageScore?.let { String.format("%.1f", it) } ?: "??"} ★",
                    style = Typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getUserMediaForMedia(media: Media, viewModel: HomeViewModel): com.kanlist.domain.model.UserMedia? {
    // This would need to be implemented to fetch the user media for this media
    return null
}

private fun java.time.LocalDateTime.formatTime(): String {
    return java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(this)
}