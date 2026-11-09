package com.kanlist.presentation.ui.profile

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.ThemeMode
import com.kanlist.domain.model.TitleLanguage
import com.kanlist.domain.model.UserPreferences
import com.kanlist.presentation.theme.KanlistTheme
import com.kanlist.presentation.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val weeklyActivity by viewModel.weeklyActivity.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()

    KanlistTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header
            ProfileHeader(
                name = preferences.name,
                streak = streak,
                onNameChange = { viewModel.updateName(it) }
            )

            // Stats overview
            StatsOverview(stats = stats)

            // Activity chart (placeholder)
            ActivityChartSection(weeklyActivity = weeklyActivity)

            // Settings sections
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                item {
                    SettingsSection(
                        title = "Aparência",
                        items = listOf(
                            SettingItem(
                                title = "Tema",
                                subtitle = preferences.theme.displayName,
                                onClick = { /* show theme picker */ }
                            ),
                            SettingItem(
                                title = "Idioma dos títulos",
                                subtitle = preferences.titleLanguage.displayName,
                                onClick = { /* show language picker */ }
                            )
                        )
                    )
                }

                item {
                    SettingsSection(
                        title = "Biblioteca",
                        items = listOf(
                            SettingItem(
                                title = "Meta diária",
                                subtitle = "${preferences.dailyGoalTarget} eps/caps por dia",
                                onClick = { /* show number picker */ }
                            ),
                            SettingItem(
                                title = "Idiomas dos capítulos",
                                subtitle = preferences.chapterLanguages.joinToString(", ") { it.displayName },
                                onClick = { /* show language multi-select */ }
                            ),
                            SettingItem(
                                title = "Traduzir sinopses automaticamente",
                                subtitle = if (preferences.autoTranslateSynopsis) "Ativado" else "Desativado",
                                trailing = {
                                    Switch(
                                        checked = preferences.autoTranslateSynopsis,
                                        onCheckedChange = { viewModel.updateAutoTranslateSynopsis(it) }
                                    )
                                }
                            ),
                            SettingItem(
                                title = "Vibração ao marcar",
                                subtitle = if (preferences.hapticFeedback) "Ativado" else "Desativado",
                                trailing = {
                                    Switch(
                                        checked = preferences.hapticFeedback,
                                        onCheckedChange = { viewModel.updateHapticFeedback(it) }
                                    )
                                }
                            ),
                            SettingItem(
                                title = "Mostrar conteúdo adulto",
                                subtitle = if (preferences.showAdultContent) "Ativado" else "Desativado",
                                trailing = {
                                    Switch(
                                        checked = preferences.showAdultContent,
                                        onCheckedChange = { viewModel.updateShowAdultContent(it) }
                                    )
                                }
                            ),
                            SettingItem(
                                title = "Fontes de dados ativas",
                                subtitle = preferences.enabledSources.joinToString(", ") { it.displayName },
                                onClick = { /* show source multi-select */ }
                            )
                        )
                    )
                }

                item {
                    SettingsSection(
                        title = "Dados",
                        items = listOf(
                            SettingItem(
                                title = "Exportar backup",
                                subtitle = "Salva toda a biblioteca em JSON",
                                trailing = { Icon(imageVector = androidx.compose.material.icons.outlined.Download, contentDescription = "") },
                                onClick = { viewModel.exportBackup() }
                            ),
                            SettingItem(
                                title = "Importar backup",
                                subtitle = "Restaura biblioteca de um JSON",
                                trailing = { Icon(imageVector = androidx.compose.material.icons.outlined.Upload, contentDescription = "") },
                                onClick = { /* show file picker */ }
                            ),
                            SettingItem(
                                title = "Limpar cache",
                                subtitle = "Remove dados temporários",
                                trailing = { Icon(imageVector = androidx.compose.material.icons.outlined.DeleteSweep, contentDescription = "") },
                                onClick = { viewModel.clearCache() }
                            ),
                            SettingItem(
                                title = "Apagar todos os dados",
                                subtitle = "Remove biblioteca, progresso e configurações",
                                textColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                trailing = { Icon(imageVector = androidx.compose.material.icons.outlined.DeleteForever, contentDescription = "", tint = androidx.compose.material3.MaterialTheme.colorScheme.error) },
                                onClick = { /* show confirmation dialog */ viewModel.clearAllData() }
                            )
                        )
                    )
                }

                item {
                    SettingsSection(
                        title = "Sobre",
                        items = listOf(
                            SettingItem(
                                title = "Versão",
                                subtitle = "1.0.0"
                            ),
                            SettingItem(
                                title = "Código aberto",
                                subtitle = "GitHub",
                                trailing = { Icon(imageVector = androidx.compose.material.icons.outlined.OpenInNew, contentDescription = "") },
                                onClick = { /* open GitHub */ }
                            ),
                            SettingItem(
                                title = "Privacidade",
                                subtitle = "Política de privacidade",
                                onClick = { /* open privacy policy */ }
                            ),
                            SettingItem(
                                title = "Licenças",
                                subtitle = "Bibliotecas de terceiros",
                                onClick = { /* show licenses */ }
                            )
                        )
                    )
                }
            }

            // Backup state indicator
            when (backupState) {
                is ProfileViewModel.BackupState.Exporting -> {
                    BackupProgressBar(progress = backupState.progress)
                }
                is ProfileViewModel.BackupState.Importing -> {
                    BackupProgressBar(progress = backupState.progress)
                }
                is ProfileViewModel.BackupState.Success -> {
                    BackupSnackbar(message = backupState.message)
                }
                is ProfileViewModel.BackupState.Error -> {
                    BackupSnackbar(message = backupState.message, isError = true)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, streak: Int, onNameChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material.icons.outlined.AccountCircle(
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                    size = 80.dp
                )
            }

            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(
                    text = if (name.isNotBlank()) name else "Usuário",
                    style = Typography.headlineMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                )
                androidx.compose.material3.IconButton(onClick = { /* show edit name dialog */ }) {
                    androidx.compose.material.Icon(
                        imageVector = androidx.compose.material.icons.outlined.Edit,
                        contentDescription = "Editar nome",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatBox(label = "Sequência", value = "$streak dias", icon = androidx.compose.material.icons.outlined.LocalFireDepartment)
                StatBox(label = "Tempo total", value = viewModel.formatTime(stats.totalTimeSpentMinutes), icon = androidx.compose.material.icons.outlined.AccessTime)
                StatBox(label = "Obras", value = stats.totalMediaInLibrary.toString(), icon = androidx.compose.material.icons.outlined.LibraryBooks)
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, icon: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer, size = 24.dp)
        }
        androidx.compose.material3.Text(text = value, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer)
        androidx.compose.material3.Text(text = label, style = Typography.labelSmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
fun StatsOverview(stats: com.kanlist.domain.model.UserStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(text = "Estatísticas", style = Typography.titleMedium)

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Episódios",
                    value = stats.totalEpisodesWatched.toString(),
                    icon = androidx.compose.material.icons.outlined.PlayCircle
                )
                StatCard(
                    title = "Capítulos",
                    value = stats.totalChaptersRead.toString(),
                    icon = androidx.compose.material.icons.outlined.MenuBook
                )
                StatCard(
                    title = "Tempo",
                    value = viewModel.formatTime(stats.totalTimeSpentMinutes),
                    icon = androidx.compose.material.icons.outlined.AccessTime
                )
                StatCard(
                    title = "Favoritos",
                    value = stats.favoriteCount.toString(),
                    icon = androidx.compose.material.icons.outlined.Favorite
                )
            }

            // By type
            if (stats.mediaByType.isNotEmpty()) {
                androidx.compose.material3.Text(text = "Por tipo", style = Typography.titleSmall)
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stats.mediaByType.toList().sortedByDescending { it.second }.forEach { (type, count) ->
                        TypeStatChip(type = type, count = count)
                    }
                }
            }

            // By status
            if (stats.mediaByStatus.isNotEmpty()) {
                androidx.compose.material3.Text(text = "Por status", style = Typography.titleSmall)
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stats.mediaByStatus.toList().sortedBy { it.key.order }.forEach { (status, count) ->
                        StatusStatChip(status = status, count = count)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, size = 24.dp)
        }
        androidx.compose.material3.Text(text = value, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        androidx.compose.material3.Text(text = title, style = Typography.labelSmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TypeStatChip(type: MediaType, count: Int) {
    Chip(
        onClick = { },
        modifier = Modifier.padding(0.dp),
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            androidx.compose.material.Icon(
                imageVector = type.icon,
                contentDescription = "",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                size = 16.dp
            )
            Text(text = "$count ${type.displayNamePlural}", style = Typography.labelSmall)
        }
    }
}

@Composable
fun StatusStatChip(status: MediaStatus, count: Int) {
    Chip(
        onClick = { },
        modifier = Modifier.padding(0.dp),
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(text = "${status.getDisplayName(MediaType.ANIME)}: $count", style = Typography.labelSmall)
    }
}

@Composable
fun ActivityChartSection(weeklyActivity: Map<java.time.LocalDate, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(text = "Atividade (14 dias)", style = Typography.titleMedium)

            // Placeholder for chart
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "Gráfico de atividade (implementar com Compose Chart)",
                    style = Typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, items: List<SettingItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = Typography.titleMedium)
            }

            androidx.compose.foundation.layout.Column {
                items.forEachIndexed { index, item ->
                    SettingRow(
                        item = item,
                        isFirst = index == 0,
                        isLast = index == items.lastIndex
                    )
                    if (index < items.lastIndex) {
                        androidx.compose.foundation.layout.Divider(
                            modifier = Modifier.padding(start = 72.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

data class SettingItem(
    val title: String,
    val subtitle: String,
    val trailing: @Composable (() -> Unit)? = null,
    val onClick: (() -> Unit)? = null,
    val textColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
)

@Composable
fun SettingRow(item: SettingItem, isFirst: Boolean, isLast: Boolean) {
    val shape = when {
        isFirst && isLast -> androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        isFirst -> androidx.compose.foundation.shape.RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        isLast -> androidx.compose.foundation.shape.RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
        else -> androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
    }

    androidx.compose.material3.TextButton(
        onClick = item.onClick ?: {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        enabled = item.onClick != null || item.trailing != null
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = item.title, style = Typography.bodyMedium, color = item.textColor)
                Text(text = item.subtitle, style = Typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item.trailing?.invoke()
        }
    }
}

@Composable
fun BackupProgressBar(progress: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.LinearProgressIndicator(progress = progress)
            androidx.compose.material3.Text(
                text = if (progress < 1f) "Processando... ${(progress * 100).roundToInt()}%" else "Concluído!",
                style = Typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun BackupSnackbar(message: String, isError: Boolean = false) {
    // Would show snackbar
}