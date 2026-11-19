package com.charlenferreira.prism.ui.gallery

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.charlenferreira.prism.R
import com.charlenferreira.prism.data.model.PhotoItem
import com.charlenferreira.prism.ui.components.LiquidGlassSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class EditorAppCandidate(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap,
    val supportsRaw: Boolean
)

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
    var showEditAppDialog by remember { mutableStateOf(false) }
    var isCurrentPageZoomed by remember { mutableStateOf(false) }

    val exifMetadata by viewModel.selectedExif.collectAsStateWithLifecycle()
    val isLoadingExif by viewModel.isLoadingExif.collectAsStateWithLifecycle()

    val currentPhoto = photos.getOrElse(pagerState.currentPage) { photos.first() }

    val headerDateLabel = remember(currentPhoto.id, currentPhoto.dateAddedSeconds) {
        val millis = if (currentPhoto.dateAddedSeconds > 0L) {
            currentPhoto.dateAddedSeconds * 1000L
        } else {
            System.currentTimeMillis()
        }
        val ptBr = Locale("pt", "BR")
        val date = SimpleDateFormat("d 'de' MMM", ptBr).format(Date(millis))
        val time = SimpleDateFormat("HH:mm", ptBr).format(Date(millis))
        "$date  •  $time"
    }

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

        // Topo flutuante minimalista estilo Fototeca iOS
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(120)),
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
                    darkFrostedOverride = true,
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("viewer_close_button")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.action_close),
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                LiquidGlassSurface(
                    shape = CircleShape,
                    darkFrostedOverride = true,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentPhoto.isRaw) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "RAW",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = headerDateLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Barra inferior estilo Fototeca iOS
        AnimatedVisibility(
            visible = showChrome,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(120)),
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
                    darkFrostedOverride = true
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.loadExif(currentPhoto)
                                    showExifSheet = true
                                }
                                .padding(horizontal = 20.dp, vertical = 13.dp)
                                .testTag("viewer_exif_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.action_exif),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Detalhes",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        if (!currentPhoto.isVideo) {
                            VerticalDivider(
                                modifier = Modifier.height(22.dp),
                                thickness = 0.6.dp,
                                color = Color.White.copy(alpha = 0.18f)
                            )

                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showEditAppDialog = true }
                                    .padding(horizontal = 20.dp, vertical = 13.dp)
                                    .testTag("viewer_edit_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.action_edit),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.action_edit),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                LiquidGlassSurface(
                    shape = CircleShape,
                    darkFrostedOverride = true,
                    onClick = { shareMediaItem(context, currentPhoto) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("viewer_share_button")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Compartilhar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
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

    if (showEditAppDialog) {
        EditAppPickerDialog(
            photo = currentPhoto,
            onDismiss = { showEditAppDialog = false },
            onSelectApp = { candidate ->
                showEditAppDialog = false
                launchSpecificEditor(context, currentPhoto, candidate)
            },
            onOpenSystemChooser = {
                showEditAppDialog = false
                openExternalEditorChooser(context, currentPhoto)
            }
        )
    }
}

@Composable
private fun EditAppPickerDialog(
    photo: PhotoItem,
    onDismiss: () -> Unit,
    onSelectApp: (EditorAppCandidate) -> Unit,
    onOpenSystemChooser: () -> Unit
) {
    val context = LocalContext.current
    var showAllEditorsEvenIfRaw by remember(photo.id) { mutableStateOf(false) }

    val allInstalledEditors = remember(photo.id, photo.mimeType) {
        queryAllInstalledImageEditors(context, photo)
    }

    val displayedEditors = remember(allInstalledEditors, photo.isRaw, showAllEditorsEvenIfRaw) {
        if (photo.isRaw && !showAllEditorsEvenIfRaw) {
            val rawCompatible = allInstalledEditors.filter { it.supportsRaw }
            rawCompatible.ifEmpty { allInstalledEditors }
        } else {
            allInstalledEditors
        }
    }

    val dialogTitle = if (photo.isRaw) {
        "Selecionar editor compatível com RAW"
    } else {
        "Selecionar editor de foto"
    }

    val dialogSubtitle = if (photo.isRaw) {
        if (allInstalledEditors.any { it.supportsRaw } && !showAllEditorsEvenIfRaw) {
            "Exibindo apenas editores instalados compatíveis com arquivos RAW (.dng). Escolha um app para revelar esta foto:"
        } else {
            "Nenhum editor dedicado de DNG foi filtrado automaticamente. Escolha qual editor instalado deseja testar:"
        }
    } else {
        "Identificamos os editores de imagem instalados no aparelho. Escolha onde deseja editar esta foto:"
    }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_app_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dialogSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (displayedEditors.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_editor_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        items(
                            items = displayedEditors,
                            key = { "${it.packageName}/${it.activityName}" }
                        ) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                    .clickable { onSelectApp(app) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("editor_app_${app.packageName}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Image(
                                        bitmap = app.icon,
                                        contentDescription = app.label,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (app.supportsRaw) {
                                            Text(
                                                text = "Compatível com RAW / DNG",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (photo.isRaw && allInstalledEditors.size > displayedEditors.size) {
                    TextButton(
                        onClick = { showAllEditorsEvenIfRaw = true },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(text = "Mostrar todos os editores (${allInstalledEditors.size})")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onOpenSystemChooser,
                        modifier = Modifier.testTag("edit_popup_chooser_button")
                    ) {
                        Text(text = "Seletor do Android")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("edit_popup_cancel_button")
                    ) {
                        Text(text = stringResource(R.string.edit_popup_cancel))
                    }
                }
            }
        }
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
                                dampingRatio = 0.78f,
                                stiffness = 360f
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
                                dampingRatio = 0.78f,
                                stiffness = 360f
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
                darkFrostedOverride = true,
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

private fun shareMediaItem(context: Context, photo: PhotoItem) {
    val mime = photo.mimeType.ifBlank { if (photo.isVideo) "video/*" else "image/*" }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, photo.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(sendIntent, null))
    } catch (_: ActivityNotFoundException) {
    }
}

private fun queryAllInstalledImageEditors(
    context: Context,
    photo: PhotoItem
): List<EditorAppCandidate> {
    val pm = context.packageManager

    // 1. Consulta editores que declaram suporte explícito a DNG / RAW
    val rawPackages = mutableSetOf<String>()
    val rawMimes = listOf("image/x-adobe-dng", "image/dng", "image/raw")
    for (rawMime in rawMimes) {
        val rawIntent = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(photo.uri, rawMime)
        }
        queryActivitiesCompat(pm, rawIntent).forEach { info ->
            rawPackages.add(info.activityInfo.packageName)
        }
    }

    // Pacotes conhecidos de revelação RAW/DNG no Android
    val knownRawEditorPackages = setOf(
        "com.niksoftware.snapseed",
        "com.adobe.lrmobile",
        "com.adobe.psmobile",
        "com.tssystems.photomate3",
        "com.tssystems.photomate2",
        "com.t8rin.imagetoolbox",
        "ru.tech.imageresizershrinker",
        "com.lightricks.photoleap",
        "com.vsco.cam"
    )

    // 2. Consulta todos os editores de imagem instalados no dispositivo
    val generalEditIntent = Intent(Intent.ACTION_EDIT).apply {
        setDataAndType(photo.uri, "image/*")
    }
    val specificMimeIntent = if (photo.mimeType.isNotBlank() && photo.mimeType != "image/*") {
        Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(photo.uri, photo.mimeType)
        }
    } else {
        null
    }

    val combinedResolved = buildList {
        addAll(queryActivitiesCompat(pm, generalEditIntent))
        if (specificMimeIntent != null) {
            addAll(queryActivitiesCompat(pm, specificMimeIntent))
        }
    }

    return combinedResolved
        .filter { it.activityInfo.packageName != context.packageName }
        .distinctBy { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
        .mapNotNull { info ->
            runCatching {
                val pkg = info.activityInfo.packageName
                val label = info.loadLabel(pm).toString()
                val iconBitmap = info.loadIcon(pm).toBitmap(width = 96, height = 96).asImageBitmap()
                val isRawCapable = pkg in rawPackages || pkg in knownRawEditorPackages
                EditorAppCandidate(
                    label = label,
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    icon = iconBitmap,
                    supportsRaw = isRawCapable
                )
            }.getOrNull()
        }
        .sortedByDescending { it.supportsRaw }
}

private fun queryActivitiesCompat(
    pm: PackageManager,
    intent: Intent
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    pm.queryIntentActivities(
        intent,
        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
    )
} else {
    @Suppress("DEPRECATION")
    pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
}

private fun launchSpecificEditor(
    context: Context,
    photo: PhotoItem,
    candidate: EditorAppCandidate
) {
    val editIntent = Intent(Intent.ACTION_EDIT).apply {
        setDataAndType(photo.uri, photo.mimeType.ifBlank { "image/*" })
        component = ComponentName(candidate.packageName, candidate.activityName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
    try {
        context.startActivity(editIntent)
    } catch (_: ActivityNotFoundException) {
        openExternalEditorChooser(context, photo)
    }
}

private fun openExternalEditorChooser(context: Context, photo: PhotoItem) {
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
