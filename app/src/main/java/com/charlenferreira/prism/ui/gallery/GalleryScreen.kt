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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewComfy
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.charlenferreira.prism.R
import com.charlenferreira.prism.data.model.PhotoItem
import com.charlenferreira.prism.data.repository.SettingsRepository
import com.charlenferreira.prism.ui.components.LiquidGlassSurface
import com.charlenferreira.prism.ui.components.LocalPrismBackdrop
import com.charlenferreira.prism.ui.components.PrismBackdropState
import com.charlenferreira.prism.ui.components.ProgressiveTopBlurScrim
import com.charlenferreira.prism.ui.components.prismBackdropSource
import com.charlenferreira.prism.ui.theme.LocalPrismColors
import java.util.Locale

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onPhotoClick: (Long) -> Unit,
    sharedBackdropState: PrismBackdropState? = null,
    onScrollTick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridColumns by viewModel.gridColumns.collectAsStateWithLifecycle()
    val rawFilterActive by viewModel.rawFilterEnabled.collectAsStateWithLifecycle()
    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    var isScrolledFloating by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    val backdropState = sharedBackdropState ?: LocalPrismBackdrop.current

    // Notify shared backdrop draw phase on scroll without causing recompositions
    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
        onScrollTick?.invoke()
        if (gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset <= 18) {
            isScrolledFloating = false
        }
    }

    // NestedScrollConnection detects gallery scroll to hide the status/info counter pill
    // and transition filter/grid controls into a floating frosted blur state with top gradient.
    val galleryScrollConnection = remember(gridState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy < -6f && (gridState.firstVisibleItemIndex > 0 ||
                        gridState.firstVisibleItemScrollOffset > 20)
                ) {
                    if (!isScrolledFloating) {
                        isScrolledFloating = true
                        isFilterMenuExpanded = false
                    }
                } else if (dy > 10f) {
                    if (isScrolledFloating) {
                        isScrolledFloating = false
                    }
                }
                return Offset.Zero
            }
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

    val scrimAlpha by animateFloatAsState(
        targetValue = if (isScrolledFloating) 1f else 0.42f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 420f),
        label = "topBlurScrimAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(galleryScrollConnection)
    ) {
        // Layer 1: Backdrop-recorded full-bleed gallery grid (no LiquidGlassSurface children inside)
        val gridContainerModifier = if (backdropState != null) {
            Modifier
                .fillMaxSize()
                .prismBackdropSource(backdropState)
        } else {
            Modifier.fillMaxSize()
        }

        Box(modifier = gridContainerModifier) {
            when (val state = uiState) {
                GalleryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }

                GalleryUiState.PermissionRequest -> {
                    CompositionLocalProvider(LocalPrismBackdrop provides null) {
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
                }

                is GalleryUiState.Error -> {
                    CompositionLocalProvider(LocalPrismBackdrop provides null) {
                        GalleryStatusCard(
                            icon = Icons.Rounded.BrokenImage,
                            title = stringResource(R.string.error_title),
                            subtitle = state.message,
                            actionLabel = stringResource(R.string.retry_button),
                            onAction = { viewModel.refreshMedia() }
                        )
                    }
                }

                is GalleryUiState.Empty -> {
                    CompositionLocalProvider(LocalPrismBackdrop provides null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = statusBarTopPadding + 70.dp)
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
                }

                is GalleryUiState.Success -> {
                    PinchablePhotoGrid(
                        photos = state.photos,
                        gridState = gridState,
                        gridColumns = gridColumns,
                        topInsetPadding = statusBarTopPadding + if (state.isPartialAccess) 120.dp else 72.dp,
                        bottomInsetPadding = navBarBottomPadding + 98.dp,
                        onGridColumnsChanged = { newColumns ->
                            viewModel.setGridColumns(newColumns)
                        },
                        onPhotoClick = onPhotoClick
                    )
                }
            }
        }

        // Layer 2: Partial access banner (sibling overlay so it sits cleanly above the grid)
        val successState = uiState as? GalleryUiState.Success
        if (successState?.isPartialAccess == true) {
            AnimatedVisibility(
                visible = !isScrolledFloating,
                enter = fadeIn(tween(180)) + slideInVertically { -it },
                exit = fadeOut(tween(140)) + slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = statusBarTopPadding + 68.dp)
            ) {
                PartialAccessBanner(
                    onManage = { permissionLauncher.launch(requiredMediaPermissions()) }
                )
            }
        }

        // Layer 3: Progressive backdrop blur + black-to-transparent top gradient scrim
        ProgressiveTopBlurScrim(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(statusBarTopPadding + 104.dp)
                .graphicsLayer { alpha = scrimAlpha }
        )

        // Layer 4: Status/Info pill (auto-hides on scroll) + Floating filter/density controls
        FloatingGalleryControls(
            uiState = uiState,
            rawFilterActive = rawFilterActive,
            gridColumns = gridColumns,
            isScrolledFloating = isScrolledFloating,
            onToggleDensity = { viewModel.toggleGridDensity() },
            onFilterIconClick = { isFilterMenuExpanded = !isFilterMenuExpanded },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        )

        // Layer 5: Compact floating corner filter popover
        AnimatedVisibility(
            visible = isFilterMenuExpanded,
            enter = fadeIn(tween(150)) +
                scaleIn(
                    initialScale = 0.88f,
                    transformOrigin = TransformOrigin(1f, 0f),
                    animationSpec = spring(dampingRatio = 0.80f, stiffness = 480f)
                ) +
                expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 480f)
                ),
            exit = fadeOut(tween(120)) +
                scaleOut(
                    targetScale = 0.90f,
                    transformOrigin = TransformOrigin(1f, 0f),
                    animationSpec = spring(dampingRatio = 0.84f, stiffness = 500f)
                ) +
                shrinkVertically(shrinkTowards = Alignment.Top),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = statusBarTopPadding + 66.dp, end = 16.dp)
        ) {
            LiquidGlassSurface(
                shape = RoundedCornerShape(22.dp),
                cornerRadiusDp = 22.dp,
                modifier = Modifier
                    .width(216.dp)
                    .testTag("filter_popover_menu")
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterPopoverItem(
                        label = stringResource(R.string.filter_all_media),
                        selected = !rawFilterActive,
                        testTag = "filter_option_all",
                        onClick = {
                            if (rawFilterActive) viewModel.toggleRawFilter()
                            isFilterMenuExpanded = false
                        }
                    )
                    FilterPopoverItem(
                        label = stringResource(R.string.filter_raw_only),
                        selected = rawFilterActive,
                        testTag = "filter_option_raw",
                        onClick = {
                            if (!rawFilterActive) viewModel.toggleRawFilter()
                            isFilterMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingGalleryControls(
    uiState: GalleryUiState,
    rawFilterActive: Boolean,
    gridColumns: Int,
    isScrolledFloating: Boolean,
    onToggleDensity: () -> Unit,
    onFilterIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompactDensity = gridColumns >= 4
    val counterText = when (uiState) {
        is GalleryUiState.Success -> {
            val prefix = if (rawFilterActive) "RAW • " else ""
            "$prefix${uiState.photos.size} itens • ${gridColumns}×"
        }
        is GalleryUiState.Empty -> {
            if (rawFilterActive) "RAW • 0 itens" else "0 itens • ${gridColumns}×"
        }
        is GalleryUiState.Loading -> "..."
        else -> "${gridColumns}×"
    }

    val controlsScale by animateFloatAsState(
        targetValue = if (isScrolledFloating) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 460f),
        label = "floatingControlsScale"
    )
    val filterClusterPadding by animateDpAsState(
        targetValue = if (isScrolledFloating) 4.dp else 10.dp,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 460f),
        label = "floatingClusterGap"
    )

    Box(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Left status/info counter pill — hides cleanly on downward scroll via NestedScrollConnection
        AnimatedVisibility(
            visible = !isScrolledFloating,
            enter = fadeIn(tween(180)) +
                slideInVertically(spring(dampingRatio = 0.84f, stiffness = 440f)) { -it / 2 } +
                expandHorizontally(expandFrom = Alignment.Start),
            exit = fadeOut(tween(130)) +
                slideOutVertically(spring(dampingRatio = 0.86f, stiffness = 480f)) { -it / 2 } +
                shrinkHorizontally(shrinkTowards = Alignment.Start),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            LiquidGlassSurface(
                shape = CircleShape,
                cornerRadiusDp = 20.dp,
                modifier = Modifier.testTag("gallery_info_status_pill")
            ) {
                AnimatedContent(
                    targetState = counterText,
                    transitionSpec = {
                        fadeIn(tween(140)) togetherWith fadeOut(tween(110))
                    },
                    label = "counterPillTransition"
                ) { text ->
                    Text(
                        text = text,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            }
        }

        // Right controls: transition seamlessly into a compact floating frosted state when scrolled
        Row(
            horizontalArrangement = Arrangement.spacedBy(filterClusterPadding),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    scaleX = controlsScale
                    scaleY = controlsScale
                }
        ) {
            LiquidGlassSurface(
                shape = CircleShape,
                cornerRadiusDp = 24.dp,
                tintOverride = if (isCompactDensity) MaterialTheme.colorScheme.primary else null,
                onClick = onToggleDensity,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("grid_density_fab")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = isCompactDensity,
                        transitionSpec = {
                            (fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.78f,
                                animationSpec = spring(dampingRatio = 0.80f, stiffness = 500f)
                            )) togetherWith (fadeOut(tween(100)) + scaleOut(targetScale = 0.78f))
                        },
                        label = "densityFabIconTransition"
                    ) { compact ->
                        Icon(
                            imageVector = if (compact) Icons.Rounded.GridView else Icons.Rounded.ViewComfy,
                            contentDescription = stringResource(
                                if (compact) {
                                    R.string.grid_density_toggle_comfortable
                                } else {
                                    R.string.grid_density_toggle_compact
                                }
                            ),
                            tint = if (compact) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            LiquidGlassSurface(
                shape = CircleShape,
                cornerRadiusDp = 24.dp,
                tintOverride = if (rawFilterActive) MaterialTheme.colorScheme.primary else null,
                onClick = onFilterIconClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("raw_filter_button")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.filter_menu_description),
                        tint = if (rawFilterActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    if (rawFilterActive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(7.dp)
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
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp)
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
        cornerRadiusDp = 18.dp,
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
    val prismColors = LocalPrismColors.current
    var currentColumnsState by remember { mutableStateOf(gridColumns) }
    LaunchedEffect(gridColumns) {
        currentColumnsState = gridColumns
    }

    val targetSpacing = when {
        gridColumns <= 3 -> 8.dp
        gridColumns == 4 -> 5.dp
        else -> 3.dp
    }
    val targetCornerRadius = when {
        gridColumns <= 3 -> 16.dp
        gridColumns == 4 -> 12.dp
        else -> 8.dp
    }
    val animatedSpacing by animateDpAsState(
        targetValue = targetSpacing,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 420f),
        label = "gridSpacingAnim"
    )
    val animatedCornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 420f),
        label = "gridCornerAnim"
    )
    val borderBrush = remember(prismColors.glassBorderStart, prismColors.glassBorderEnd) {
        Brush.linearGradient(
            colors = listOf(
                prismColors.glassBorderStart.copy(alpha = 0.42f),
                Color.Transparent,
                prismColors.glassBorderEnd.copy(alpha = 0.30f)
            )
        )
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(gridColumns),
        contentPadding = PaddingValues(
            start = 10.dp,
            end = 10.dp,
            top = topInsetPadding,
            bottom = bottomInsetPadding
        ),
        horizontalArrangement = Arrangement.spacedBy(animatedSpacing),
        verticalArrangement = Arrangement.spacedBy(animatedSpacing),
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
            key = { it.id }
        ) { photo ->
            PhotoGridTile(
                photo = photo,
                cornerRadius = animatedCornerRadius,
                borderBrush = borderBrush,
                onClick = { onPhotoClick(photo.id) },
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(160),
                    fadeOutSpec = tween(120),
                    placementSpec = spring<IntOffset>(
                        dampingRatio = 0.84f,
                        stiffness = 420f
                    )
                )
            )
        }
    }
}

@Composable
private fun PhotoGridTile(
    photo: PhotoItem,
    cornerRadius: Dp,
    borderBrush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prismColors = LocalPrismColors.current
    val tileShape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val imageRequest = remember(photo.id, photo.uri) {
        ImageRequest.Builder(context)
            .data(photo.uri)
            .crossfade(80)
            .build()
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(tileShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(
                width = 0.75.dp,
                brush = borderBrush,
                shape = tileShape
            )
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
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(prismColors.rawBadgeContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "RAW",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall,
                    color = prismColors.rawBadgeContent
                )
            }
        }

        if (photo.isVideo) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
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
            shape = RoundedCornerShape(28.dp),
            cornerRadiusDp = 28.dp,
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
            shape = RoundedCornerShape(28.dp),
            cornerRadiusDp = 28.dp,
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
