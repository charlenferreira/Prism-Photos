package com.charlenferreira.prism.data.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class PhotoItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val dateAddedSeconds: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val durationMs: Long = 0L,
    val isRaw: Boolean = false,
    val isVideo: Boolean = false,
    val bucketName: String = ""
)

@Immutable
data class ExifMetadata(
    val model: String,
    val lens: String,
    val aperture: String,
    val iso: String,
    val shutterSpeed: String,
    val focalLength: String,
    val resolution: String,
    val fileSize: String
)
