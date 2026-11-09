package com.kanlist.presentation.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaType
import com.kanlist.presentation.navigation.Screen
import com.kanlist.presentation.theme.KanlistTheme
import com.kanlist.presentation.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel(),
    onNavigateToWorkDetail: (String) -> Unit
) {
    val tab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(ExploreViewModel.Tab.WATCH) }
    val trendingWatch by viewModel.trendingWatch.collectAsStateWithLifecycle()
    val seasonalWatch by viewModel.seasonalWatch.collectAsStateWithLifecycle()
    val popularManhwa by viewModel.popularManhwa.collectAsStateWithLifecycle()
    val popularDonghua by viewModel.popularDonghua.collectAsStateWithLifecycle()
    val trendingRead by viewModel.trendingRead.collectAsStateWithLifecycle()
    val popularManga by viewModel.popularManga.collectAsStateWithLifecycle()
    val popularManhwaRead by viewModel.popularManhwaRead.collectAsStateWithLifecycle()
    val popularManhua by viewModel.popularManhua.collectAsStateWithLifecycle()
    val popularNovels by viewModel.popularNovels.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    KanlistTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab bar
            TabBar(
                selectedTab = tab,
                onTabClick = { tab = it },
                tabs = listOf(
                    ExploreViewModel.Tab.WATCH to "Assistir",
                    ExploreViewModel.Tab.READ to "Ler"
                )
            )

            // Content
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                when (tab) {
                    ExploreViewModel.Tab.WATCH -> ExploreWatchContent(
                        trending = trendingWatch.getOrNull()?.media ?: emptyList(),
                        seasonal = seasonalWatch.getOrNull()?.media ?: emptyList(),
                        manhwa = popularManhwa.getOrNull()?.media ?: emptyList(),
                        donghua = popularDonghua.getOrNull()?.media ?: emptyList(),
                        onItemClick = onNavigateToWorkDetail
                    )
                    ExploreViewModel.Tab.READ -> ExploreReadContent(
                        trending = trendingRead.getOrNull()?.media ?: emptyList(),
                        manga = popularManga.getOrNull()?.media ?: emptyList(),
                        manhwa = popularManhwaRead.getOrNull()?.media ?: emptyList(),
                        manhua = popularManhua.getOrNull()?.media ?: emptyList(),
                        novels = popularNovels.getOrNull()?.media ?: emptyList(),
                        onItemClick = onNavigateToWorkDetail
                    )
                }
            }
        }
    }
}

@Composable
fun TabBar(
    selectedTab: ExploreViewModel.Tab,
    onTabClick: (ExploreViewModel.Tab) -> Unit,
    tabs: List<Pair<ExploreViewModel.Tab, String>>
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (tab, label) ->
            val isSelected = selectedTab == tab
            androidx.compose.material3.TextButton(
                onClick = { onTabClick(tab) },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                colors = androidx.compose.material3.TextButtonDefaults.textButtonColors(
                    contentColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary
                    else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                androidx.compose.material3.Text(
                    text = label,
                    style = Typography.labelLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                )
            }
        }
    }
}

@Composable
fun ExploreWatchContent(
    trending: List<Media>,
    seasonal: List<Media>,
    manhwa: List<Media>,
    donghua: List<Media>,
    onItemClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            SectionHeader(title = "Em alta", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = trending, onItemClick = onItemClick)
        }

        item {
            SectionHeader(title = "Temporada atual", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = seasonal, onItemClick = onItemClick)
        }

        item {
            SectionHeader(title = "Manhwas populares", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = manhwa, onItemClick = onItemClick)
        }

        item {
            SectionHeader(title = "Donghuas populares", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = donghua, onItemClick = onItemClick)
        }
    }
}

@Composable
fun ExploreReadContent(
    trending: List<Media>,
    manga: List<Media>,
    manhwa: List<Media>,
    manhua: List<Media>,
    novels: List<Media>,
    onItemClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            SectionHeader(title = "Em alta", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = trending, onItemClick = onItemClick)
        }

        item {
            SectionHeader(title = "Mangás populares", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = manga, onItemClick = onItemClick)
        }

        item {
            SectionHeader(title = "Manhwas", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = manhwa, onItemClick = onItemClick)
        }

        item {
            SectionHeader(title = "Manhuas", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = manhua, onItemClick = onItemClick)
        }

        item {
            SectionHeader(title = "Novels & Light Novels", seeAllAction = { /* navigate */ })
        }
        item {
            MediaHorizontalList(media = novels, onItemClick = onItemClick)
        }
    }
}

@Composable
fun SectionHeader(title: String, seeAllAction: () -> Unit) {
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
}

@Composable
fun MediaHorizontalList(media: List<Media>, onItemClick: (String) -> Unit) {
    androidx.compose.foundation.layout.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(media.take(10)) { item ->
            MediaCard(media = item, onClick = { onItemClick(item.id) })
        }
    }
}

@Composable
fun MediaCard(media: Media, onClick: () -> Unit) {
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
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material.icons.outlined.Image(
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 48.dp
                )
            }

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(horizontal = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.Text(
                    text = media.displayTitle,
                    style = Typography.titleSmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                )
                androidx.compose.material3.Text(
                    text = media.type.displayName,
                    style = Typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}