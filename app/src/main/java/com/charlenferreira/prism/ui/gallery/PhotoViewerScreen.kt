package com.charlenferreira.prism.ui.gallery

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.charlenferreira.prism.R
import com.charlenferreira.prism.data.model.PhotoItem
import com.charlenferreira.prism.ui.components.LiquidGlassSurface
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PhotoViewerScreen(
    photos: List<PhotoItem>,
    initialPhotoId: Long,
    viewModel: GalleryViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val initialIndex = remember(photos, initialPhotoId) {
        photos.indexOfFirst { it.id == initialPhotoId }.coerceAtLeast(0)
    }

    if (photos.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )
    var showChrome by remember { mutableStateOf(true) }
    var showExifSheet by remember { mutableStateOf(false) }
    var isCurrentPageZoomed by remember { mutableStateOf(false) }

    val exifMetadata by viewModel.selectedExif.collectAsStateWithLifecycle()
    val isLoadingExif by viewModel.isLoadingExif.collectAsStateWithLifecycle()

    val currentPhoto = photos.getOrElse(pagerState.currentPage) { photos.first() }

    LaunchedEffect(pagerState.currentPage) {
        isCurrentPageZoomed = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isCurrentPageZoomed,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val photo = photos[pageIndex]
            ZoomablePhotoPage(
                photo = photo,
                onTap = { showChrome = !showChrome },
                onZoomStateChanged = { zoomed ->
                    if (pageIndex == pagerState.currentPage) {
                        isCurrentPageZoomed = zoomed
                    }
                },
                onPlayVideo = { openExternalVideoPlayer(context, photo) }
            )
        }

        // Top floating Liquid Glass bar (Close + filename/RAW badge)
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassSurface(
                    shape = CircleShape,
                    cornerRadiusDp = 24.dp,
                    tintOverride = Color(0xFF141824),
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("viewer_close_button")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                LiquidGlassSurface(
                    shape = CircleShape,
                    cornerRadiusDp = 24.dp,
                    tintOverride = Color(0xFF141824),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentPhoto.isRaw) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "RAW",
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Text(
                            text = currentPhoto.displayName,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bottom floating Liquid Glass actions: EXIF + Edit
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassSurface(
                    shape = CircleShape,
                    cornerRadiusDp = 28.dp,
                    tintOverride = Color(0xFF141824),
                    onClick = {
                        viewModel.loadExif(currentPhoto)
                        showExifSheet = true
                    },
                    modifier = Modifier.testTag("viewer_exif_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = stringResource(R.string.action_exif),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_exif),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                if (!currentPhoto.isVideo) {
                    LiquidGlassSurface(
                        shape = CircleShape,
                        cornerRadiusDp = 28.dp,
                        tintOverride = Color(0xFF141824),
                        onClick = { openExternalEditor(context, currentPhoto) },
                        modifier = Modifier.testTag("viewer_edit_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.action_edit),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.action_edit),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExifSheet) {
        ExifBottomSheet(
            photo = currentPhoto,
            exifMetadata = exifMetadata,
            isLoading = isLoadingExif,
            onDismiss = {
                showExifSheet = false
                viewModel.clearExif()
            }
        )
    }
}

@Composable
private fun ZoomablePhotoPage(
    photo: PhotoItem,
    onTap: () -> Unit,
    onZoomStateChanged: (Boolean) -> Unit,
    onPlayVideo: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scale = remember(photo.id) { Animatable(1f) }
    val offsetX = remember(photo.id) { Animatable(0f) }
    val offsetY = remember(photo.id) { Animatable(0f) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(scale.value) {
        onZoomStateChanged(scale.value > 1.02f)
    }

    fun clampOffset(targetScale: Float, rawX: Float, rawY: Float): Offset {
        if (targetScale <= 1f || viewportSize == IntSize.Zero) return Offset.Zero
        val maxX = ((targetScale - 1f) * viewportSize.width) / 2f
        val maxY = ((targetScale - 1f) * viewportSize.height) / 2f
        return Offset(
            x = rawX.coerceIn(-maxX, maxX),
            y = rawY.coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { viewportSize = it }
            .pointerInput(photo.id) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { tapOffset ->
                        scope.launch {
                            val springSpec = spring<Float>(
                                dampingRatio = 0.6f,
                                stiffness = 800f
                            )
                            if (scale.value > 1.15f) {
                                launch { scale.animateTo(1f, springSpec) }
                                launch { offsetX.animateTo(0f, springSpec) }
                                launch { offsetY.animateTo(0f, springSpec) }
                            } else {
                                val targetScale = 2.5f
                                val centerX = viewportSize.width / 2f
                                val centerY = viewportSize.height / 2f
                                val rawOffset = Offset(
                                    x = (centerX - tapOffset.x) * (targetScale - 1f),
                                    y = (centerY - tapOffset.y) * (targetScale - 1f)
                                )
                                val clamped = clampOffset(targetScale, rawOffset.x, rawOffset.y)
                                launch { scale.animateTo(targetScale, springSpec) }
                                launch { offsetX.animateTo(clamped.x, springSpec) }
                                launch { offsetY.animateTo(clamped.y, springSpec) }
                            }
                        }
                    }
                )
            }
            .pointerInput(photo.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var zoomGestureActive = false
                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.count { it.pressed }
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        if (pointerCount >= 2 || scale.value > 1.02f) {
                            if (pointerCount >= 2 || abs(zoomChange - 1f) > 0.01f) {
                                zoomGestureActive = true
                            }
                            if (zoomGestureActive || scale.value > 1.02f) {
                                val newScale = (scale.value * zoomChange).coerceIn(1f, 5f)
                                val clamped = clampOffset(
                                    targetScale = newScale,
                                    rawX = offsetX.value + panChange.x,
                                    rawY = offsetY.value + panChange.y
                                )
                                scope.launch {
                                    scale.snapTo(newScale)
                                    offsetX.snapTo(clamped.x)
                                    offsetY.snapTo(clamped.y)
                                }
                                event.changes.forEach { change ->
                                    if (change.positionChanged()) {
                                        change.consume()
                                    }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (scale.value <= 1.02f) {
                        scope.launch {
                            val springSpec = spring<Float>(
                                dampingRatio = 0.6f,
                                stiffness = 800f
                            )
                            launch { scale.animateTo(1f, springSpec) }
                            launch { offsetX.animateTo(0f, springSpec) }
                            launch { offsetY.animateTo(0f, springSpec) }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.uri)
                .crossfade(true)
                .build(),
            contentDescription = photo.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                }
        )

        if (photo.isVideo) {
            LiquidGlassSurface(
                shape = CircleShape,
                cornerRadiusDp = 36.dp,
                tintOverride = Color(0xFF141824),
                onClick = onPlayVideo,
                modifier = Modifier
                    .size(72.dp)
                    .testTag("viewer_play_video_button")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.action_play_video),
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

private fun openExternalEditor(context: Context, photo: PhotoItem) {
    val editIntent = Intent(Intent.ACTION_EDIT).apply {
        setDataAndType(photo.uri, photo.mimeType.ifBlank { "image/*" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(
        editIntent,
        context.getString(R.string.chooser_edit_title)
    )
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.no_editor_available),
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun openExternalVideoPlayer(context: Context, photo: PhotoItem) {
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(photo.uri, photo.mimeType.ifBlank { "video/*" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(viewIntent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.no_video_player_available),
            Toast.LENGTH_SHORT
        ).show()
    }
}
