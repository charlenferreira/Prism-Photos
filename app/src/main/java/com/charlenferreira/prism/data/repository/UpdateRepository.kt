package com.charlenferreira.prism.data.repository

import com.charlenferreira.prism.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val releaseName: String,
    val releaseNotes: String,
    val downloadUrl: String
)

class UpdateRepository {

    suspend fun checkForUpdate(
        currentVersion: String = BuildConfig.VERSION_NAME
    ): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(GITHUB_LATEST_RELEASE_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Prism-Android/$currentVersion")
            }

            try {
                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                    return@runCatching null
                }
                if (code !in 200..299) {
                    error("GitHub API HTTP $code")
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val tagName = json.optString("tag_name", "").trim()
                val htmlUrl = json.optString("html_url", "").trim()
                val releaseName = json.optString("name", tagName).ifBlank { tagName }
                val releaseNotes = json.optString("body", "").trim()

                if (tagName.isBlank() || htmlUrl.isBlank()) {
                    return@runCatching null
                }

                val cleanRemote = normalizeVersion(tagName)
                if (isNewerVersion(remoteVersion = cleanRemote, currentVersion = currentVersion)) {
                    UpdateInfo(
                        latestVersion = cleanRemote,
                        releaseName = releaseName,
                        releaseNotes = releaseNotes,
                        downloadUrl = htmlUrl
                    )
                } else {
                    null
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    companion object {
        const val GITHUB_LATEST_RELEASE_URL =
            "https://api.github.com/repos/charlenferreira/Prism/releases/latest"
        private const val TIMEOUT_MS = 10_000

        fun normalizeVersion(version: String): String {
            return version.trim()
                .removePrefix("v")
                .removePrefix("V")
                .substringBefore('-')
                .substringBefore('+')
                .trim()
        }

        fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
            val remoteParts = normalizeVersion(remoteVersion)
                .split('.')
                .mapNotNull { it.toIntOrNull() }
            val currentParts = normalizeVersion(currentVersion)
                .split('.')
                .mapNotNull { it.toIntOrNull() }

            if (remoteParts.isEmpty() || currentParts.isEmpty()) return false

            val maxSize = maxOf(remoteParts.size, currentParts.size)
            for (index in 0 until maxSize) {
                val remote = remoteParts.getOrElse(index) { 0 }
                val current = currentParts.getOrElse(index) { 0 }
                if (remote > current) return true
                if (remote < current) return false
            }
            return false
        }
    }
}
