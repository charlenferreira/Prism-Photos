package com.charlenferreira.prism.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charlenferreira.prism.BuildConfig
import com.charlenferreira.prism.R
import com.charlenferreira.prism.data.repository.LiquidGlassConfig
import com.charlenferreira.prism.data.repository.LiquidGlassPreset
import com.charlenferreira.prism.data.repository.SettingsRepository
import com.charlenferreira.prism.data.repository.UpdateInfo
import com.charlenferreira.prism.ui.components.LiquidGlassSurface
import com.charlenferreira.prism.ui.components.PrismBackdropState
import com.charlenferreira.prism.ui.components.ProgressiveTopBlurScrim
import com.charlenferreira.prism.ui.components.prismBackdropSource
import com.charlenferreira.prism.ui.theme.PrismTheme
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    sharedBackdropState: PrismBackdropState? = null,
    onScrollTick: () -> Unit = {}
) {
    val selectedTheme by viewModel.theme.collectAsStateWithLifecycle()
    val gridColumns by viewModel.gridColumns.collectAsStateWithLifecycle()
    val glassConfig by viewModel.glassConfig.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
    val statusMessage by viewModel.updateStatusMessage.collectAsStateWithLifecycle()

    var fineTuneExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow { selectedTheme to glassConfig }
            .collect { onScrollTick() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val listModifier = if (sharedBackdropState != null) {
            Modifier
                .fillMaxSize()
                .prismBackdropSource(sharedBackdropState)
        } else {
            Modifier.fillMaxSize()
        }

        LazyColumn(
            modifier = listModifier.testTag("settings_list"),
            contentPadding = PaddingValues(
                top = 96.dp,
                bottom = 124.dp,
                start = 18.dp,
                end = 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Seletor dos 4 Temas Minimalistas
            item(key = "section_themes") {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(26.dp),
                    cornerRadiusDp = 26.dp,
                    config = glassConfig,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            icon = Icons.Rounded.Palette,
                            title = "Aparência & Temas",
                            subtitle = "4 paletas minimalistas calibradas"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PrismTheme.entries.chunked(2).forEach { rowThemes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowThemes.forEach { themeItem ->
                                        ThemeCard(
                                            theme = themeItem,
                                            selected = themeItem == selectedTheme,
                                            onClick = { viewModel.selectTheme(themeItem) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Painel "Liquid glass & motion" (Presets + Fine-tune)
            item(key = "section_liquid_glass") {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(26.dp),
                    cornerRadiusDp = 26.dp,
                    config = glassConfig,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            icon = Icons.Rounded.AutoAwesome,
                            title = "Liquid glass & motion",
                            subtitle = "Motor óptico Kyant0 Backdrop"
                        )

                        // 4 Presets Rápidos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LiquidGlassPreset.entries.forEach { preset ->
                                val isSelected = glassConfig.preset == preset
                                val pillBg by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    },
                                    animationSpec = tween(180),
                                    label = "presetBg"
                                )
                                val pillText by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    animationSpec = tween(180),
                                    label = "presetText"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(pillBg)
                                        .clickable { viewModel.applyGlassPreset(preset) }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = preset.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = pillText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Botão Expansível Fine-tune
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .clickable { fineTuneExpanded = !fineTuneExpanded }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ajuste fino (Fine-tune)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = if (fineTuneExpanded) {
                                    Icons.Rounded.ExpandLess
                                } else {
                                    Icons.Rounded.ExpandMore
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = fineTuneExpanded) {
                            FineTuneControls(
                                config = glassConfig,
                                onConfigChange = { viewModel.updateGlassConfig(it) }
                            )
                        }
                    }
                }
            }

            // 3. Densidade da Grade (3 a 5 colunas)
            item(key = "section_grid") {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(26.dp),
                    cornerRadiusDp = 26.dp,
                    config = glassConfig,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(
                            icon = Icons.Rounded.GridView,
                            title = "Densidade da Fototeca",
                            subtitle = "Colunas na grade principal ($gridColumns colunas)"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            (SettingsRepository.MIN_COLUMNS..SettingsRepository.MAX_COLUMNS).forEach { cols ->
                                val isSelected = gridColumns == cols
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                        )
                                        .clickable { viewModel.selectGridColumns(cols) }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$cols colunas",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Atualizações do GitHub (charlenferreira/Prism-Photos)
            item(key = "section_updates") {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(26.dp),
                    cornerRadiusDp = 26.dp,
                    config = glassConfig,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(
                            icon = Icons.Rounded.SystemUpdate,
                            title = "Atualizações do Prism",
                            subtitle = "GitHub Releases • charlenferreira/Prism-Photos"
                        )

                        Button(
                            onClick = { viewModel.checkForUpdates(manual = true) },
                            enabled = !isCheckingUpdate,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("check_updates_button")
                        ) {
                            if (isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Verificando...")
                            } else {
                                Text("Verificar atualizações")
                            }
                        }

                        if (statusMessage != null) {
                            val statusText = when (statusMessage) {
                                "up_to_date" -> "Você já está na versão mais recente (v${BuildConfig.VERSION_NAME})."
                                "error" -> "Não foi possível conectar ao GitHub agora."
                                else -> statusMessage.orEmpty()
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 5. Rodapé com Versão
            item(key = "footer_version") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Prism Photos • v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Cabeçalho flutuante superior
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            ProgressiveTopBlurScrim(height = 92.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        updateInfo?.let { info ->
            UpdateAvailableDialog(
                updateInfo = info,
                onDismiss = { viewModel.dismissUpdateDialog() }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: PrismTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        },
        animationSpec = tween(180),
        label = "themeBorder"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(theme.swatchPrimary, theme.swatchSecondary)
                        )
                    )
                    .border(1.dp, Color.Gray.copy(alpha = 0.35f), CircleShape)
            )

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Column {
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = theme.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FineTuneControls(
    config: LiquidGlassConfig,
    onConfigChange: (LiquidGlassConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassSliderRow(
            label = "Frost (Desfoque)",
            valueText = "${config.frostDp.roundToInt()} dp",
            value = config.frostDp,
            valueRange = 0f..28f,
            onValueChange = { onConfigChange(config.copy(frostDp = it)) }
        )

        GlassSliderRow(
            label = "Lens depth",
            valueText = "${config.lensDepthDp.roundToInt()} dp",
            value = config.lensDepthDp,
            valueRange = 0f..32f,
            onValueChange = { onConfigChange(config.copy(lensDepthDp = it)) }
        )

        GlassSliderRow(
            label = "Lens strength",
            valueText = "${config.lensStrengthDp.roundToInt()} dp",
            value = config.lensStrengthDp,
            valueRange = 0f..48f,
            onValueChange = { onConfigChange(config.copy(lensStrengthDp = it)) }
        )

        GlassSliderRow(
            label = "Tint (Opacidade)",
            valueText = "${(config.tintAlpha * 100).roundToInt()}%",
            value = config.tintAlpha,
            valueRange = 0.10f..0.85f,
            onValueChange = { onConfigChange(config.copy(tintAlpha = it)) }
        )

        GlassSliderRow(
            label = "Rim light (Borda)",
            valueText = "${(config.rimLightAlpha * 100).roundToInt()}%",
            value = config.rimLightAlpha,
            valueRange = 0.10f..1.0f,
            onValueChange = { onConfigChange(config.copy(rimLightAlpha = it)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Depth (Sombra de profundidade)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = config.depthShadow,
                onCheckedChange = { onConfigChange(config.copy(depthShadow = it)) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prism edge (Borda iridescente)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Usa mais GPU (recomendado desligado)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = config.prismEdge,
                onCheckedChange = { onConfigChange(config.copy(prismEdge = it)) }
            )
        }
    }
}

@Composable
private fun GlassSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun UpdateAvailableDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.update_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            val summary = buildString {
                append("Prism v${updateInfo.latestVersion}")
                if (updateInfo.releaseNotes.isNotBlank()) {
                    append("\n\n")
                    append(updateInfo.releaseNotes.take(280))
                }
            }
            Text(
                text = summary,
                fontFamily = FontFamily.SansSerif,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        // Ignore if no browser is installed
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("update_download_button")
            ) {
                Text(text = stringResource(R.string.update_dialog_download))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("update_later_button")
            ) {
                Text(text = stringResource(R.string.update_dialog_later))
            }
        }
    )
}
