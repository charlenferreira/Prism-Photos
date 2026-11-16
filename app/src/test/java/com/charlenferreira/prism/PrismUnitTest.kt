package com.charlenferreira.prism

import com.charlenferreira.prism.data.repository.PhotoRepository
import com.charlenferreira.prism.data.repository.UpdateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrismUnitTest {

    @Test
    fun `isNewerVersion handles semver tags with and without v prefix`() {
        assertTrue(UpdateRepository.isNewerVersion("v1.0.2", "1.0.1"))
        assertTrue(UpdateRepository.isNewerVersion("v1.0.10", "1.0.2"))
        assertTrue(UpdateRepository.isNewerVersion("2.0.0", "1.9.9"))
        assertFalse(UpdateRepository.isNewerVersion("v1.0.1", "1.0.1"))
        assertFalse(UpdateRepository.isNewerVersion("v1.0.0", "1.0.1"))
    }

    @Test
    fun `isRawFormat detects DNG and camera RAW files accurately`() {
        assertTrue(PhotoRepository.isRawFormat("DSC_0042.DNG", "image/x-adobe-dng"))
        assertTrue(PhotoRepository.isRawFormat("IMG_9001.cr3", "application/octet-stream"))
        assertTrue(PhotoRepository.isRawFormat("photo.arw", "image/x-sony-arw"))
        assertFalse(PhotoRepository.isRawFormat("IMG_1234.jpg", "image/jpeg"))
        assertFalse(PhotoRepository.isRawFormat("screenshot.png", "image/png"))
    }

    @Test
    fun `formatShutterSpeed formats fractional and multi-second exposures`() {
        assertEquals("1/250s", PhotoRepository.formatShutterSpeed(0.004))
        assertEquals("1/60s", PhotoRepository.formatShutterSpeed(1.0 / 60.0))
        assertEquals("2s", PhotoRepository.formatShutterSpeed(2.0))
        assertEquals("1.5s", PhotoRepository.formatShutterSpeed(1.5))
    }

    @Test
    fun `formatByteSize produces human readable file sizes`() {
        assertEquals("512 B", PhotoRepository.formatByteSize(512L))
        assertEquals("2.0 KB", PhotoRepository.formatByteSize(2048L))
        assertEquals("3.50 MB", PhotoRepository.formatByteSize((3.5 * 1024 * 1024).toLong()))
    }
}
