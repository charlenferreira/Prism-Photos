package com.charlenferreira.prism.ui.gallery

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ViewComfy
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.charlenferreira.prism.R
import com.charlenferreira.prism.data.model.PhotoItem
import com.charlenferreira.prism.data.repository.SettingsRepository
import com.charlenferreira.prism.ui.components.LiquidGlassSurface
import com.charlenferreira.prism.ui.components.PrismBackdropState
import com.charlenferreira.prism.ui.components.ProgressiveTopBlurScrim
import com.charlenferreira.prism.ui.theme.LocalPrismColors
import java.util.Locale

private enum class MediaCategoryTab(val label: String) {
    RAW_DNG("RAW / DNG"),
    VIDEOS("Vídeos"),
    PHOTOS("Fotos")
}

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onPhotoClick: (Long) -> Unit,
    @Suppress("UNUSED_PARAMETER") sharedBackdropState: PrismBackdropState? = null,
    @Suppress("UNUSED_PARAMETER") onScrollTick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridColumns by viewModel.gridColumns.collectAsStateWithLifecycle()
    val rawFilterActive by viewModel.rawFilterEnabled.collectAsStateWithLifecycle()

    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    // null = exibe todas as mídias; tocar numa aba filtra e tocar de novo limpa o filtro
    var selectedCategory by rememberSaveable { mutableStateOf<MediaCategoryTab?>(null) }
    val gridState = rememberLazyGridState()

    // Sincroniza se o usuário ativar o filtro RAW pelo menu superior
    LaunchedEffect(rawFilterActive) {
        if (rawFilterActive) {
            selectedCategory = MediaCategoryTab.RAW_DNG
        } else if (selectedCategory == MediaCategoryTab.RAW_DNG) {
            selectedCategory = null
        }
    }

    fun syncPermissions() {
        val (full, partial) = checkMediaPermissions(context)
        viewModel.onPermissionStatusUpdated(hasFullAccess = full, hasPartialAccess = partial)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        syncPermissions()
    }

    LaunchedEffect(Unit) {
        syncPermissions()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                syncPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Filtra a lista localmente em memória (zero peso na GPU/banco)
    val filteredPhotos = remember(uiState, selectedCategory) {
        val all = (uiState as? GalleryUiState.Success)?.photos.orEmpty()
        when (selectedCategory) {
            null -> all
            MediaCategoryTab.RAW_DNG -> all.filter { it.isRaw }
            MediaCategoryTab.VIDEOS -> all.filter { it.isVideo }
            MediaCategoryTab.PHOTOS -> all.filter { !it.isVideo && !it.isRaw }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            GalleryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp
                    )
                }
            }

            GalleryUiState.PermissionRequest -> {
                PermissionRequestCard(
                    onRequestPermission = {
                        permissionLauncher.launch(requiredMediaPermissions())
                    },
                    onOpenSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                )
            }

            is GalleryUiState.Error -> {
                GalleryStatusCard(
                    icon = Icons.Rounded.BrokenImage,
                    title = stringResource(R.string.error_title),
                    subtitle = state.message,
                    actionLabel = stringResource(R.string.retry_button),
                    onAction = { viewModel.refreshMedia() }
                )
            }

            is GalleryUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = statusBarTopPadding + 84.dp)
                ) {
                    if (state.isPartialAccess) {
                        PartialAccessBanner(
                            onManage = { permissionLauncher.launch(requiredMediaPermissions()) }
                        )
                    }
                    GalleryStatusCard(
                        icon = Icons.Rounded.PhotoLibrary,
                        title = stringResource(
                            if (state.isRawFilterActive) R.string.empty_raw_title else R.string.empty_title
                        ),
                        subtitle = stringResource(
                            if (state.isRawFilterActive) {
                                R.string.empty_raw_description
                            } else {
                                R.string.empty_description
                            }
                        ),
                        actionLabel = if (state.isRawFilterActive) {
                            stringResource(R.string.filter_all_media)
                        } else {
                            stringResource(R.string.retry_button)
                        },
                        onAction = {
                            if (state.isRawFilterActive) {
                                viewModel.toggleRawFilter()
                            } else {
                                viewModel.refreshMedia()
                            }
                        }
                    )
                }
            }

            is GalleryUiState.Success -> {
                val topPadding = statusBarTopPadding + if (state.isPartialAccess) 128.dp else 82.dp
                val bottomPadding = navBarBottomPadding + 144.dp

                if (filteredPhotos.isEmpty()) {
                    GalleryStatusCard(
                        icon = Icons.Rounded.PhotoLibrary,
                        title = "Nenhum item em ${selectedCategory?.label ?: "Fototeca"}",
                        subtitle = "Toque abaixo para voltar a exibir toda a sua biblioteca.",
                        actionLabel = "Mostrar tudo",
                        onAction = {
                            selectedCategory = null
                            if (rawFilterActive) viewModel.toggleRawFilter()
                        }
                    )
                } else {
                    PinchablePhotoGrid(
                        photos = filteredPhotos,
                        gridState = gridState,
                        gridColumns = gridColumns,
                        topInsetPadding = topPadding,
                        bottomInsetPadding = bottomPadding,
                        onGridColumnsChanged = { newColumns ->
                            viewModel.setGridColumns(newColumns)
                        },
                        onPhotoClick = onPhotoClick
                    )
                }
            }
        }

        // Banner de acesso parcial
        val successState = uiState as? GalleryUiState.Success
        if (successState?.isPartialAccess == true) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = statusBarTopPadding + 78.dp)
            ) {
                PartialAccessBanner(
                    onManage = { permissionLauncher.launch(requiredMediaPermissions()) }
                )
            }
        }

        // Degradê superior suave
        ProgressiveTopBlurScrim(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(statusBarTopPadding + 92.dp)
        )

        // Cabeçalho estilo Fototeca iOS
        IosFototecaHeader(
            uiState = uiState,
            displayedCount = filteredPhotos.size,
            selectedCategory = selectedCategory,
            gridColumns = gridColumns,
            onToggleDensity = { viewModel.toggleGridDensity() },
            onFilterIconClick = { isFilterMenuExpanded = !isFilterMenuExpanded },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        )

        // Pílula flutuante de categorias ("RAW / DNG | Vídeos | Fotos")
        if (uiState is GalleryUiState.Success) {
            FloatingCategoryPill(
                selectedCategory = selectedCategory,
                onSelectCategory = { clicked ->
                    val next = if (selectedCategory == clicked) null else clicked
                    selectedCategory = next
                    if (next != MediaCategoryTab.RAW_DNG && rawFilterActive) {
                        viewModel.toggleRawFilter()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 82.dp)
            )
        }

        // Menu flutuante rápido no topo direito
        AnimatedVisibility(
            visible = isFilterMenuExpanded,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(100)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = statusBarTopPadding + 68.dp, end = 16.dp)
        ) {
            LiquidGlassSurface(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .width(210.dp)
                    .testTag("filter_popover_menu")
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    FilterPopoverItem(
                        label = stringResource(R.string.filter_all_media),
                        selected = selectedCategory == null,
                        testTag = "filter_option_all",
                        onClick = {
                            selectedCategory = null
                            if (rawFilterActive) viewModel.toggleRawFilter()
                            isFilterMenuExpanded = false
                        }
                    )
                    FilterPopoverItem(
                        label = "Apenas RAW (.dng)",
                        selected = selectedCategory == MediaCategoryTab.RAW_DNG,
                        testTag = "filter_option_raw",
                        onClick = {
                            selectedCategory = MediaCategoryTab.RAW_DNG
                            isFilterMenuExpanded = false
                        }
                    )
                    FilterPopoverItem(
                        label = "Apenas Vídeos",
                        selected = selectedCategory == MediaCategoryTab.VIDEOS,
                        testTag = "filter_option_videos",
                        onClick = {
                            selectedCategory = MediaCategoryTab.VIDEOS
                            if (rawFilterActive) viewModel.toggleRawFilter()
                            isFilterMenuExpanded = false
                        }
                    )
                    FilterPopoverItem(
                        label = "Apenas Fotos",
                        selected = selectedCategory == MediaCategoryTab.PHOTOS,
                        testTag = "filter_option_photos",
                        onClick = {
                            selectedCategory = MediaCategoryTab.PHOTOS
                            if (rawFilterActive) viewModel.toggleRawFilter()
                            isFilterMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IosFototecaHeader(
    uiState: GalleryUiState,
    displayedCount: Int,
    selectedCategory: MediaCategoryTab?,
    gridColumns: Int,
    onToggleDensity: () -> Unit,
    onFilterIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompactDensity = gridColumns >= 4
    val isFiltered = selectedCategory != null
    val subtitleText = when (uiState) {
        is GalleryUiState.Success -> {
            val prefix = if (selectedCategory != null) "${selectedCategory.label} • " else ""
            "$prefix$displayedCount itens"
        }
        is GalleryUiState.Empty -> "0 itens"
        is GalleryUiState.Loading -> "Carregando..."
        else -> ""
    }

    Row(
        modifier = modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Fototeca",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitleText.isNotEmpty()) {
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassSurface(
                shape = CircleShape,
                tintOverride = if (isCompactDensity) MaterialTheme.colorScheme.primary else null,
                onClick = onToggleDensity,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("grid_density_fab")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCompactDensity) Icons.Rounded.GridView else Icons.Rounded.ViewComfy,
                        contentDescription = stringResource(
                            if (isCompactDensity) {
                                R.string.grid_density_toggle_comfortable
                            } else {
                                R.string.grid_density_toggle_compact
                            }
                        ),
                        tint = if (isCompactDensity) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            LiquidGlassSurface(
                shape = CircleShape,
                tintOverride = if (isFiltered) MaterialTheme.colorScheme.primary else null,
                onClick = onFilterIconClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("raw_filter_button")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = stringResource(R.string.filter_menu_description),
                        tint = if (isFiltered) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    if (isFiltered) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingCategoryPill(
    selectedCategory: MediaCategoryTab?,
    onSelectCategory: (MediaCategoryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val prismColors = LocalPrismColors.current
    val activeChipBg = if (prismColors.isDarkSurface) {
        Color.White.copy(alpha = 0.16f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    LiquidGlassSurface(
        shape = CircleShape,
        contentPadding = 4.dp,
        modifier = modifier.height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            MediaCategoryTab.entries.forEach { tab ->
                val isSelected = selectedCategory == tab
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) activeChipBg else Color.Transparent)
                        .clickable { onSelectCategory(tab) }
                        .padding(horizontal = 13.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPopoverItem(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PartialAccessBanner(onManage: () -> Unit) {
    LiquidGlassSurface(
        shape = RoundedCornerShape(18.dp),
        onClick = onManage,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("partial_access_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.partial_access_banner),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.partial_access_manage),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PinchablePhotoGrid(
    photos: List<PhotoItem>,
    gridState: LazyGridState,
    gridColumns: Int,
    topInsetPadding: Dp,
    bottomInsetPadding: Dp,
    onGridColumnsChanged: (Int) -> Unit,
    onPhotoClick: (Long) -> Unit
) {
    var currentColumnsState by remember { mutableStateOf(gridColumns) }
    LaunchedEffect(gridColumns) {
        currentColumnsState = gridColumns
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(gridColumns),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = topInsetPadding,
            bottom = bottomInsetPadding
        ),
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalArrangement = Arrangement.spacedBy(1.5.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("photo_grid")
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var cumulativeZoom = 1f
                    var switchedInGesture = false
                    do {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 2) {
                            val zoom = event.calculateZoom()
                            cumulativeZoom *= zoom
                            if (!switchedInGesture) {
                                if (cumulativeZoom >= 1.24f &&
                                    currentColumnsState > SettingsRepository.MIN_COLUMNS
                                ) {
                                    val next = currentColumnsState - 1
                                    currentColumnsState = next
                                    onGridColumnsChanged(next)
                                    switchedInGesture = true
                                } else if (cumulativeZoom <= 0.80f &&
                                    currentColumnsState < SettingsRepository.MAX_COLUMNS
                                ) {
                                    val next = currentColumnsState + 1
                                    currentColumnsState = next
                                    onGridColumnsChanged(next)
                                    switchedInGesture = true
                                }
                            }
                            event.changes.forEach { it.consume() }
                        } else {
                            cumulativeZoom = 1f
                            switchedInGesture = false
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        items(
            items = photos,
            key = { it.id },
            contentType = { "photo_tile" }
        ) { photo ->
            PhotoGridTile(
                photo = photo,
                onClick = { onPhotoClick(photo.id) }
            )
        }
    }
}

@Composable
private fun PhotoGridTile(
    photo: PhotoItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val prismColors = LocalPrismColors.current
    val imageRequest = remember(photo.id) {
        ImageRequest.Builder(context)
            .data(photo.uri)
            .size(260)
            .build()
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .testTag("photo_tile_${photo.id}")
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = photo.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (photo.isRaw) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(prismColors.rawBadgeContainer)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "RAW",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = prismColors.rawBadgeContent
                )
            }
        }

        if (photo.isVideo) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = formatDuration(photo.durationMs),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PermissionRequestCard(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.permission_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.permission_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grant_permission_button")
                ) {
                    Text(text = stringResource(R.string.permission_grant))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_app_settings_button")
                ) {
                    Text(text = stringResource(R.string.permission_open_settings))
                }
            }
        }
    }
}

@Composable
private fun GalleryStatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = 72.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.testTag("status_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = actionLabel)
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "0:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

fun requiredMediaPermissions(): Array<String> {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

fun checkMediaPermissions(context: Context): Pair<Boolean, Boolean> {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            val imagesGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val videoGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
            val partialGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED
            Pair(imagesGranted || videoGranted, partialGranted)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            val imagesGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val videoGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
            Pair(imagesGranted || videoGranted, false)
        }
        else -> {
            val storageGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            Pair(storageGranted, false)
        }
    }
}
