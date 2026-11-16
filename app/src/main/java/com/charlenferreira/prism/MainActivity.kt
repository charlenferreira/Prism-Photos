package com.charlenferreira.prism

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charlenferreira.prism.data.repository.UpdateInfo
import com.charlenferreira.prism.ui.gallery.GalleryViewModel
import com.charlenferreira.prism.ui.navigation.PrismNavGraph
import com.charlenferreira.prism.ui.settings.SettingsViewModel
import com.charlenferreira.prism.ui.theme.PrismAppTheme

class MainActivity : ComponentActivity() {

    private val app: PrismApplication
        get() = application as PrismApplication

    private val galleryViewModel: GalleryViewModel by viewModels {
        GalleryViewModel.Factory(
            photoRepository = app.photoRepository,
            settingsRepository = app.settingsRepository
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            settingsRepository = app.settingsRepository,
            updateRepository = app.updateRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTheme by settingsViewModel.theme.collectAsStateWithLifecycle()
            val updateInfo by settingsViewModel.updateInfo.collectAsStateWithLifecycle()

            PrismAppTheme(prismTheme = selectedTheme) {
                PrismNavGraph(
                    galleryViewModel = galleryViewModel,
                    settingsViewModel = settingsViewModel
                )

                updateInfo?.let { info ->
                    UpdateAvailableDialog(
                        updateInfo = info,
                        onDismiss = { settingsViewModel.dismissUpdateDialog() }
                    )
                }
            }
        }
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
