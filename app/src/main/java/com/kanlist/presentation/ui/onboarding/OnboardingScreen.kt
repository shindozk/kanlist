package com.kanlist.presentation.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanlist.presentation.theme.KanlistTheme
import com.kanlist.presentation.theme.MediaTileBackgroundColor
import com.kanlist.presentation.theme.MediaTileIconColor
import com.kanlist.domain.model.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onComplete: () -> Unit
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current

    KanlistTheme {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Mascot/Logo area
                Card(
                    modifier = Modifier.size(120.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MediaTileBackgroundColor(MediaType.ANIME)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material.icons.outlined.Movie(
                            tint = MediaTileIconColor(MediaType.ANIME),
                            size = 60.dp
                        )
                    }
                }

                // Welcome text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bem-vindo ao Kanlist!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    androidx.compose.foundation.layout.spacer(androidx.compose.ui.unit.Dp(8))
                    Text(
                        text = "Seu diário de animes e mangás.\nOrganize o que assiste e lê, sem conta e offline.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Name input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Como devemos te chamar?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        androidx.compose.foundation.layout.spacer(androidx.compose.ui.unit.Dp(16))
                        TextField(
                            value = name,
                            onValueChange = { viewModel.setName(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Seu nome ou apelido") },
                            singleLine = true,
                            isError = error != null,
                            supportingText = { error?.let { Text(it) } },
                            colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Continue button
                Button(
                    onClick = { viewModel.completeOnboarding(onComplete) },
                    enabled = name.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Text(text = "Começar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Features preview
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureRow(icon = androidx.compose.material.icons.outlined.NoAccounts, text = "Sem cadastro, sem login")
                    FeatureRow(icon = androidx.compose.material.icons.outlined.CloudOff, text = "Funciona offline")
                    FeatureRow(icon = androidx.compose.material.icons.outlined.PrivacyTip, text = "Seus dados ficam no seu celular")
                    FeatureRow(icon = androidx.compose.material.icons.outlined.AutoAwesome, text = "Múltiplas fontes: AniList, MangaDex, Kitsu, MAL")
                }
            }
        }
    }
}

@Composable
fun FeatureRow(icon: @Composable () -> Unit, text: String) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, size = 24.dp)
        }
        Text(text = text, fontSize = 14.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPreview() {
    OnboardingScreen(onComplete = {})
}