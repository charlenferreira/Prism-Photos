package com.charlenferreira.prism.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.charlenferreira.prism.data.model.ExifMetadata
import com.charlenferreira.prism.data.model.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

class PhotoRepository(private val context: Context) {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    suspend fun loadMedia(): List<PhotoItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<PhotoItem>()
        items.addAll(queryImages())
        items.addAll(queryVideos())
        items.sortWith(
            compareByDescending<PhotoItem> { it.dateAddedSeconds }
                .thenByDescending { it.id }
        )
        items
    }

    private fun queryImages(): List<PhotoItem> {
        val result = mutableListOf<PhotoItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
            val dateCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
            val widthCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
            val bucketCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = if (nameCol >= 0) cursor.getString(nameCol).orEmpty() else ""
                val mime = if (mimeCol >= 0) cursor.getString(mimeCol).orEmpty() else "image/*"
                val dateAdded = if (dateCol >= 0) cursor.getLong(dateCol) else 0L
                val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                val width = if (widthCol >= 0) cursor.getInt(widthCol) else 0
                val height = if (heightCol >= 0) cursor.getInt(heightCol) else 0
                val bucket = if (bucketCol >= 0) cursor.getString(bucketCol).orEmpty() else ""

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                result.add(
                    PhotoItem(
                        id = id,
                        uri = contentUri,
                        displayName = name.ifBlank { "IMG_$id" },
                        mimeType = mime.ifBlank { "image/*" },
                        dateAddedSeconds = dateAdded,
                        sizeBytes = size,
                        width = width,
                        height = height,
                        durationMs = 0L,
                        isRaw = isRawFormat(name, mime),
                        isVideo = false,
                        bucketName = bucket
                    )
                )
            }
        }
        return result
    }

    private fun queryVideos(): List<PhotoItem> {
        val result = mutableListOf<PhotoItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
            val dateCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
            val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
            val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
            val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
            val bucketCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = if (nameCol >= 0) cursor.getString(nameCol).orEmpty() else ""
                val mime = if (mimeCol >= 0) cursor.getString(mimeCol).orEmpty() else "video/*"
                val dateAdded = if (dateCol >= 0) cursor.getLong(dateCol) else 0L
                val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                val width = if (widthCol >= 0) cursor.getInt(widthCol) else 0
                val height = if (heightCol >= 0) cursor.getInt(heightCol) else 0
                val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                val bucket = if (bucketCol >= 0) cursor.getString(bucketCol).orEmpty() else ""

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                result.add(
                    PhotoItem(
                        id = id,
                        uri = contentUri,
                        displayName = name.ifBlank { "VID_$id" },
                        mimeType = mime.ifBlank { "video/*" },
                        dateAddedSeconds = dateAdded,
                        sizeBytes = size,
                        width = width,
                        height = height,
                        durationMs = duration,
                        isRaw = false,
                        isVideo = true,
                        bucketName = bucket
                    )
                )
            }
        }
        return result
    }

    suspend fun readExifMetadata(photo: PhotoItem): ExifMetadata = withContext(Dispatchers.IO) {
        val unavailable = "N/D"
        var make = ""
        var model = ""
        var lensMake = ""
        var lensModel = ""
        var aperture: String? = null
        var iso: String? = null
        var shutter: String? = null
        var focal: String? = null
        var exifWidth = photo.width
        var exifHeight = photo.height

        if (!photo.isVideo) {
            runCatching {
                contentResolver.openInputStream(photo.uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    make = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim().orEmpty()
                    model = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim().orEmpty()
                    lensMake = exif.getAttribute(ExifInterface.TAG_LENS_MAKE)?.trim().orEmpty()
                    lensModel = exif.getAttribute(ExifInterface.TAG_LENS_MODEL)?.trim().orEmpty()

                    val fNumber = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, -1.0)
                    if (fNumber > 0.0) {
                        aperture = String.format(Locale.US, "f/%.1f", fNumber)
                    } else {
                        val apertureVal = exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE)
                        if (!apertureVal.isNullOrBlank()) {
                            aperture = "f/$apertureVal"
                        }
                    }

                    val isoTag = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                        ?: exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
                    if (!isoTag.isNullOrBlank()) {
                        iso = "ISO ${isoTag.trim()}"
                    }

                    val exposureSec = exif.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, -1.0)
                    if (exposureSec > 0.0) {
                        shutter = formatShutterSpeed(exposureSec)
                    }

                    val focalMm = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, -1.0)
                    val focal35 = exif.getAttributeInt(
                        ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
                        -1
                    )
                    focal = when {
                        focalMm > 0.0 && focal35 > 0 ->
                            String.format(Locale.US, "%.1f mm (%dmm eq)", focalMm, focal35)
                        focalMm > 0.0 ->
                            String.format(Locale.US, "%.1f mm", focalMm)
                        focal35 > 0 ->
                            "${focal35} mm"
                        else -> null
                    }

                    val w = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                    val h = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
                    if (w > 0 && h > 0) {
                        exifWidth = w
                        exifHeight = h
                    }
                }
            }
        }

        val combinedModel = buildString {
            if (make.isNotBlank() && !model.startsWith(make, ignoreCase = true)) {
                append(make).append(" ")
            }
            append(model)
        }.trim().ifBlank { unavailable }

        val combinedLens = buildString {
            if (lensMake.isNotBlank() && !lensModel.startsWith(lensMake, ignoreCase = true)) {
                append(lensMake).append(" ")
            }
            append(lensModel)
        }.trim().ifBlank { unavailable }

        val resolutionText = if (exifWidth > 0 && exifHeight > 0) {
            val mp = (exifWidth.toLong() * exifHeight.toLong()) / 1_000_000.0
            if (mp >= 0.1) {
                String.format(Locale.US, "%d × %d (%.1f MP)", exifWidth, exifHeight, mp)
            } else {
                "$exifWidth × $exifHeight"
            }
        } else {
            unavailable
        }

        ExifMetadata(
            model = combinedModel,
            lens = combinedLens,
            aperture = aperture ?: unavailable,
            iso = iso ?: unavailable,
            shutterSpeed = shutter ?: unavailable,
            focalLength = focal ?: unavailable,
            resolution = resolutionText,
            fileSize = formatByteSize(photo.sizeBytes)
        )
    }

    companion object {
        private val RAW_EXTENSIONS = setOf(
            "dng", "raw", "cr2", "cr3", "nef", "arw", "orf", "rw2", "raf", "pef", "srw"
        )

        fun isRawFormat(displayName: String, mimeType: String): Boolean {
            val lowerMime = mimeType.lowercase(Locale.US)
            if (lowerMime.contains("dng") || lowerMime.contains("raw")) {
                return true
            }
            val ext = displayName.substringAfterLast('.', "").lowercase(Locale.US)
            return ext in RAW_EXTENSIONS
        }

        fun formatShutterSpeed(seconds: Double): String {
            return if (seconds >= 1.0) {
                if (seconds % 1.0 == 0.0) {
                    "${seconds.toInt()}s"
                } else {
                    String.format(Locale.US, "%.1fs", seconds)
                }
            } else {
                val denominator = (1.0 / seconds).roundToInt().coerceAtLeast(1)
                "1/${denominator}s"
            }
        }

        fun formatByteSize(bytes: Long): String {
            if (bytes <= 0L) return "0 B"
            val kb = 1024.0
            val mb = kb * 1024.0
            val gb = mb * 1024.0
            return when {
                bytes >= gb -> String.format(Locale.US, "%.2f GB", bytes / gb)
                bytes >= mb -> String.format(Locale.US, "%.2f MB", bytes / mb)
                bytes >= kb -> String.format(Locale.US, "%.1f KB", bytes / kb)
                else -> "$bytes B"
            }
        }
    }
}
