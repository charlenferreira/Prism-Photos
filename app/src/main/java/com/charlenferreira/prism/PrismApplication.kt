package com.charlenferreira.prism

import android.app.Application
import com.charlenferreira.prism.data.repository.PhotoRepository
import com.charlenferreira.prism.data.repository.SettingsRepository
import com.charlenferreira.prism.data.repository.UpdateRepository

class PrismApplication : Application() {

    val photoRepository: PhotoRepository by lazy {
        PhotoRepository(applicationContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    val updateRepository: UpdateRepository by lazy {
        UpdateRepository()
    }
}
