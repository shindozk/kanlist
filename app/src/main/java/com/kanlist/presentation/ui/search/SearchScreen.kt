package com.kanlist.presentation.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaType
import com.kanlist.presentation.navigation.Screen
import com.kanlist.presentation.theme.KanlistTheme
import com.kanlist.presentation.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onNavigateToWorkDetail: (String) -> Unit
) {
    val query by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val selectedSources by viewModel.selectedSources.collectAsStateWithLifecycle()
    val includeAdult by viewModel.includeAdult.collectAsStateWithLifecycle()

    KanlistTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { viewModel.search(it) },
                onFilterClick = { /* show filter bottom sheet */ },
                isSearching = isSearching
            )

            // Content
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                if (query.isBlank() && recentSearches.isNotEmpty()) {
                    RecentSearchesSection(
                        searches = recentSearches,
                        onSearchClick = { q ->
                            query = q
                            viewModel.search(q)
                        },
                        onClearClick = { viewModel.clearRecentSearches() }
                    )
                } else if (query.isNotBlank()) {
                    SearchResultsSection(
                        results = searchResults,
                        isSearching = isSearching,
                        onItemClick = onNavigateToWorkDetail,
                        onLoadMore = { viewModel.loadMore() },
                        onAddToLibrary = { media, status -> viewModel.addToLibrary(media, status) }
                    )
                } else {
                    EmptySearchState()
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    isSearching: Boolean
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { androidx.compose.material3.Text("Buscar animes, mangás...") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            leadingIcon = {
                androidx.compose.material.Icon(
                    imageVector = androidx.compose.material.icons.outlined.Search,
                    contentDescription = "Buscar",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    androidx.compose.material.IconButton(onClick = { onQueryChange("") }) {
                        androidx.compose.material.Icon(
                            imageVector = androidx.compose.material.icons.outlined.Close,
                            contentDescription = "Limpar",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
            )
        )

        androidx.compose.material3.IconButton(onClick = onFilterClick) {
            androidx.compose.material.Icon(
                imageVector = androidx.compose.material.icons.outlined.Tune,
                contentDescription = "Filtros",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentSearchesSection(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(text = "Buscas recentes", style = Typography.titleMedium)
            androidx.compose.material3.TextButton(onClick = onClearClick) {
                androidx.compose.material3.Text(text = "Limpar", style = Typography.labelMedium)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searches) { search ->
                SearchHistoryItem(search = search, onClick = { onSearchClick(search) })
            }
        }
    }
}

@Composable
fun SearchHistoryItem(search: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material.Icon(
                    imageVector = androidx.compose.material.icons.outlined.History,
                    contentDescription = "Histórico",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.Text(text = search, style = Typography.bodyMedium)
            }
            androidx.compose.material.Icon(
                imageVector = androidx.compose.material.icons.outlined.ArrowForwardIos,
                contentDescription = "",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                size = 20.dp
            )
        }
    }
}

@Composable
fun SearchResultsSection(
    results: Result<com.kanlist.domain.model.MediaSearchResult>,
    isSearching: Boolean,
    onItemClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onAddToLibrary: (Media, com.kanlist.domain.model.MediaStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Results header
        results.onSuccess { res ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(
                    text = "${res.media.size} resultado${if (res.media.size != 1) "s" else ""}",
                    style = Typography.titleMedium
                )
                if (res.sources.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        text = res.sources.joinToString(", ") { it.displayName },
                        style = Typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Results list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results.getOrNull()?.media ?: emptyList()) { media ->
                SearchResultItem(
                    media = media,
                    onClick = { onItemClick(media.id) },
                    onAddToLibrary = { status -> onAddToLibrary(media, status) }
                )
            }

            item {
                if (isSearching) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (results.getOrNull()?.pageInfo.hasNextPage == true) {
                    androidx.compose.material3.TextButton(onClick = onLoadMore) {
                        androidx.compose.material3.Text(text = "Carregar mais", style = Typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    media: Media,
    onClick: () -> Unit,
    onAddToLibrary: (com.kanlist.domain.model.MediaStatus) -> Unit
) {
    Card(
        onClick = onClick,
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
            // Cover
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(60.dp, 90.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material.Icon(
                    imageVector = androidx.compose.material.icons.outlined.Image,
                    contentDescription = media.displayTitle,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 32.dp
                )
            }

            // Info
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = media.displayTitle,
                        style = Typography.titleSmall,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
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
                            text = media.type.displayName,
                            style = Typography.labelSmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                androidx.compose.material3.Text(
                    text = media.title.english ?: media.title.native ?: "",
                    style = Typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                )

                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "${media.averageScore?.let { String.format("%.1f", it) } ?: "??"} ★",
                        style = Typography.bodySmall
                    )
                    if (media.totalEpisodesOrChapters > 0) {
                        androidx.compose.material3.Text(
                            text = "${media.totalEpisodesOrChapters} ${if (media.type in MediaType.watchTypes) "eps" else "caps"}",
                            style = Typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Add button
            androidx.compose.material3.MenuAnchor { anchor ->
                androidx.compose.material3.IconButton(onClick = { anchor.open() }) {
                    androidx.compose.material.Icon(
                        imageVector = androidx.compose.material.icons.outlined.Add,
                        contentDescription = "Adicionar à biblioteca",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            } popover = {
                androidx.compose.material3.DropdownMenu(
                    expanded = true,
                    onDismissRequest = { /* handled by MenuAnchor */ }
                ) {
                    com.kanlist.domain.model.MediaStatus.values().forEach { status ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { androidx.compose.material3.Text(text = status.getDisplayName(media.type)) },
                            onClick = { onAddToLibrary(status) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchState() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material.Icon(
                imageVector = androidx.compose.material.icons.outlined.SearchOff,
                contentDescription = "Sem resultados",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                size = 64.dp
            )
            androidx.compose.material3.Text(
                text = "Busque por animes, mangás, filmes...",
                style = Typography.titleMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}