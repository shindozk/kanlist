package com.kanlist.presentation.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Chip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanlist.domain.model.Collection
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.UserMedia
import com.kanlist.presentation.navigation.Screen
import com.kanlist.presentation.theme.KanlistTheme
import com.kanlist.presentation.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onNavigateToWorkDetail: (String) -> Unit
) {
    val tab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(LibraryViewModel.Tab.LIST) }
    val library by viewModel.library.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val statusCounts by viewModel.statusCounts.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    KanlistTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab bar
            TabBar(
                selectedTab = tab,
                onTabClick = { tab = it },
                tabs = listOf(
                    LibraryViewModel.Tab.LIST to "Minha lista",
                    LibraryViewModel.Tab.COLLECTIONS to "Coleções"
                )
            )

            // Content
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                when (tab) {
                    LibraryViewModel.Tab.LIST -> LibraryListContent(
                        library = library,
                        statusCounts = statusCounts,
                        filterStatus = filterStatus,
                        filterType = filterType,
                        searchQuery = searchQuery,
                        sortOrder = sortOrder,
                        onFilterStatusChange = { viewModel.setFilterStatus(it) },
                        onFilterTypeChange = { viewModel.setFilterType(it) },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onSortChange = { viewModel.setSortOrder(it) },
                        onItemClick = onNavigateToWorkDetail,
                        onIncrementProgress = { media -> viewModel.updateProgress(media) },
                        onStatusChange = { media, status -> viewModel.updateStatus(media, status) },
                        onScoreChange = { media, score -> viewModel.updateScore(media, score) },
                        onFavoriteToggle = { media -> viewModel.toggleFavorite(media) },
                        onRemove = { media -> viewModel.removeFromLibrary(media) },
                        onResetProgress = { media -> viewModel.resetProgress(media) }
                    )
                    LibraryViewModel.Tab.COLLECTIONS -> CollectionsContent(
                        collections = collections,
                        onCreateCollection = { name, color, icon -> viewModel.createCollection(name, color, icon) },
                        onUpdateCollection = { viewModel.updateCollection(it) },
                        onDeleteCollection = { viewModel.deleteCollection(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun TabBar(
    selectedTab: LibraryViewModel.Tab,
    onTabClick: (LibraryViewModel.Tab) -> Unit,
    tabs: List<Pair<LibraryViewModel.Tab, String>>
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
fun LibraryListContent(
    library: List<UserMedia>,
    statusCounts: Map<MediaStatus, Int>,
    filterStatus: MediaStatus?,
    filterType: MediaType?,
    searchQuery: String,
    sortOrder: com.kanlist.domain.model.LibrarySortOrder,
    onFilterStatusChange: (MediaStatus?) -> Unit,
    onFilterTypeChange: (MediaType?) -> Unit,
    onSearchChange: (String) -> Unit,
    onSortChange: (com.kanlist.domain.model.LibrarySortOrder) -> Unit,
    onItemClick: (String) -> Unit,
    onIncrementProgress: (UserMedia) -> Unit,
    onStatusChange: (UserMedia, MediaStatus) -> Unit,
    onScoreChange: (UserMedia, Float) -> Unit,
    onFavoriteToggle: (UserMedia) -> Unit,
    onRemove: (UserMedia) -> Unit,
    onResetProgress: (UserMedia) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Filtrar biblioteca...") },
            leadingIcon = { Icon(imageVector = androidx.compose.material.icons.outlined.Search, contentDescription = "") },
            trailingIcon = {
                if (filterStatus != null || filterType != null) {
                    IconButton(onClick = { onFilterStatusChange(null); onFilterTypeChange(null) }) {
                        Icon(imageVector = androidx.compose.material.icons.outlined.FilterListOff, contentDescription = "Limpar filtros")
                    }
                } else {
                    IconButton(onClick = { /* show filter bottom sheet */ }) {
                        Icon(imageVector = androidx.compose.material.icons.outlined.Tune, contentDescription = "Filtros")
                    }
                }
            },
            colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
            )
        )

        // Status chips
        StatusChipsRow(
            counts = statusCounts,
            selected = filterStatus,
            onSelect = onFilterStatusChange
        )

        // Sort dropdown
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.end
        ) {
            androidx.compose.material3.MenuAnchor { anchor ->
                androidx.compose.material3.TextButton(
                    onClick = { anchor.open() },
                    leadingIcon = { Icon(imageVector = androidx.compose.material.icons.outlined.Sort, contentDescription = "") }
                ) {
                    Text(text = sortOrder.displayName)
                }
            } popover = {
                androidx.compose.material3.DropdownMenu(expanded = true, onDismissRequest = {}) {
                    com.kanlist.domain.model.LibrarySortOrder.values().forEach { order ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = order.displayName) },
                            onClick = { onSortChange(order) }
                        )
                    }
                }
            }
        }

        // Library list
        if (library.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyLibraryState()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(library) { userMedia ->
                    LibraryItem(
                        userMedia = userMedia,
                        onClick = { onItemClick(userMedia.mediaId) },
                        onIncrementProgress = { onIncrementProgress(userMedia) },
                        onStatusChange = { onStatusChange(userMedia, it) },
                        onScoreChange = { onScoreChange(userMedia, it) },
                        onFavoriteToggle = { onFavoriteToggle(userMedia) },
                        onRemove = { onRemove(userMedia) },
                        onResetProgress = { onResetProgress(userMedia) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChipsRow(
    counts: Map<MediaStatus, Int>,
    selected: MediaStatus?,
    onSelect: (MediaStatus?) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All chip
        FilterChip(
            label = "Todos (${counts.values.sum()})",
            selected = selected == null,
            onClick = { onSelect(null) }
        )

        MediaStatus.values().forEach { status ->
            val count = counts[status] ?: 0
            if (count > 0) {
                FilterChip(
                    label = "${status.getDisplayName(MediaType.ANIME)} ($count)",
                    selected = selected == status,
                    onClick = { onSelect(status) }
                )
            }
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Chip(
        onClick = onClick,
        selected = selected,
        modifier = Modifier.padding(horizontal = 0.dp),
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(text = label, style = Typography.labelSmall)
    }
}

@Composable
fun LibraryItem(
    userMedia: UserMedia,
    onClick: () -> Unit,
    onIncrementProgress: () -> Unit,
    onStatusChange: (MediaStatus) -> Unit,
    onScoreChange: (Float) -> Unit,
    onFavoriteToggle: () -> Unit,
    onRemove: () -> Unit,
    onResetProgress: () -> Unit
) {
    val media = userMedia.media
    val progress = userMedia.progressPercentage
    val isWatchType = media?.type in MediaType.watchTypes

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
                    contentDescription = media?.displayTitle ?: "",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 32.dp
                )
            }

            // Info
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = media?.displayTitle ?: "Desconhecido", style = Typography.titleSmall, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
                    media?.let { m ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .background(
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = m.type.displayName, style = Typography.labelSmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )

                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress indicator
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ProgressRingSmall(progress = progress)
                        Text(text = "${progress.roundToInt()}%", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }

                    // Status
                    Text(
                        text = userMedia.status.getDisplayName(media?.type ?: MediaType.ANIME),
                        style = Typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Score
                    if (userMedia.score > 0) {
                        Text(text = "${String.format("%.1f", userMedia.score)} ★", style = Typography.bodySmall)
                    }
                }

                // Progress bar
                androidx.compose.material3.LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }

            // Actions
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // +1 Button
                androidx.compose.material3.IconButton(onClick = onIncrementProgress) {
                    Icon(imageVector = androidx.compose.material.icons.outlined.Add, contentDescription = "Avançar", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                }

                // Menu
                androidx.compose.material3.MenuAnchor { anchor ->
                    androidx.compose.material3.IconButton(onClick = { anchor.open() }) {
                        Icon(imageVector = androidx.compose.material.icons.outlined.MoreVert, contentDescription = "Mais opções")
                    }
                } popover = {
                    androidx.compose.material3.DropdownMenu(expanded = true, onDismissRequest = {}) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = "Status: ${userMedia.status.getDisplayName(media?.type ?: MediaType.ANIME)}") },
                            onClick = { /* show status picker */ }
                        )
                        MediaStatus.values().forEach { status ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(text = status.getDisplayName(media?.type ?: MediaType.ANIME)) },
                                onClick = { onStatusChange(status) }
                            )
                        }
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = userMedia.isFavorite ? "Remover dos favoritos" : "Favoritar") },
                            onClick = onFavoriteToggle
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = "Zerar progresso") },
                            onClick = onResetProgress
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = "Remover da biblioteca") },
                            onClick = onRemove
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressRingSmall(progress: Float) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(32.dp)) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = (size.width - 4.dp.value) / 2f
        
        drawCircle(
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            center = center,
            radius = radius,
            style = androidx.compose.ui.draw.Stroke(4.dp.value)
        )
        
        val sweepAngle = 360 * (progress / 100)
        drawArc(
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            center = center,
            radius = radius,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = androidx.compose.ui.draw.Stroke(4.dp.value, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun EmptyLibraryState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.material.Icon(
            imageVector = androidx.compose.material.icons.outlined.LibraryBooks,
            contentDescription = "Biblioteca vazia",
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            size = 64.dp
        )
        Text(text = "Sua biblioteca está vazia", style = Typography.titleMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "Use o botão + para adicionar obras", style = Typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CollectionsContent(
    collections: List<Collection>,
    onCreateCollection: (String, Int, String?) -> Unit,
    onUpdateCollection: (Collection) -> Unit,
    onDeleteCollection: (Collection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (collections.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material.Icon(
                        imageVector = androidx.compose.material.icons.outlined.CollectionsBookmark,
                        contentDescription = "Sem coleções",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 64.dp
                    )
                    Text(text = "Nenhuma coleção ainda", style = Typography.titleMedium)
                    Text(text = "Crie sua primeira coleção", style = Typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { /* show create dialog */ }) {
                        Text("Criar coleção")
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(collections) { collection ->
                    CollectionCard(
                        collection = collection,
                        onClick = { /* navigate to collection detail */ },
                        onEdit = { onUpdateCollection(collection) },
                        onDelete = { onDeleteCollection(collection) }
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(collection.color),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                collection.icon?.let { iconName ->
                    // Would render icon
                } ?: androidx.compose.material.Icon(
                    imageVector = androidx.compose.material.icons.outlined.Folder,
                    contentDescription = "",
                    tint = Color.White,
                    size = 24.dp
                )
            }

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = collection.name, style = Typography.titleMedium)
                Text(text = "${collection.mediaCount} obras", style = Typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }

            androidx.compose.material3.MenuAnchor { anchor ->
                androidx.compose.material3.IconButton(onClick = { anchor.open() }) {
                    Icon(imageVector = androidx.compose.material.icons.outlined.MoreVert, contentDescription = "Mais")
                }
            } popover = {
                androidx.compose.material3.DropdownMenu(expanded = true, onDismissRequest = {}) {
                    androidx.compose.material3.DropdownMenuItem(text = { Text("Editar") }, onClick = onEdit)
                    androidx.compose.material3.DropdownMenuItem(text = { Text("Excluir") }, onClick = onDelete)
                }
            }
        }
    }
}