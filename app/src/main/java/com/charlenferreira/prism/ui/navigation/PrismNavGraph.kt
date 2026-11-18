package com.charlenferreira.prism.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.charlenferreira.prism.ui.components.LiquidGlassBackground
import com.charlenferreira.prism.ui.components.LiquidGlassPillNavBar
import com.charlenferreira.prism.ui.components.LocalPrismBackdrop
import com.charlenferreira.prism.ui.components.rememberPrismBackdropState
import com.charlenferreira.prism.ui.gallery.GalleryScreen
import com.charlenferreira.prism.ui.gallery.GalleryViewModel
import com.charlenferreira.prism.ui.gallery.PhotoViewerScreen
import com.charlenferreira.prism.ui.settings.SettingsScreen
import com.charlenferreira.prism.ui.settings.SettingsViewModel
import kotlinx.serialization.Serializable

@Serializable
data object HomeTabsRoute

@Serializable
data class ViewerRoute(val photoId: Long)

@Composable
fun PrismNavGraph(
    galleryViewModel: GalleryViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isViewerRoute = currentDestination?.hasRoute<ViewerRoute>() == true

    var isGallerySelected by rememberSaveable { mutableStateOf(true) }
    val sharedBackdropState = rememberPrismBackdropState()

    CompositionLocalProvider(LocalPrismBackdrop provides sharedBackdropState) {
        LiquidGlassBackground(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = HomeTabsRoute,
                    enterTransition = { fadeIn(animationSpec = tween(140)) },
                    exitTransition = { fadeOut(animationSpec = tween(100)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(140)) },
                    popExitTransition = { fadeOut(animationSpec = tween(100)) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable<HomeTabsRoute> {
                        BackHandler(enabled = !isGallerySelected) {
                            isGallerySelected = true
                        }

                        if (isGallerySelected) {
                            GalleryScreen(
                                viewModel = galleryViewModel,
                                onPhotoClick = { photoId ->
                                    navController.navigate(ViewerRoute(photoId = photoId))
                                }
                            )
                        } else {
                            SettingsScreen(
                                viewModel = settingsViewModel
                            )
                        }
                    }

                    composable<ViewerRoute>(
                        enterTransition = { fadeIn(animationSpec = tween(150)) },
                        exitTransition = { fadeOut(animationSpec = tween(110)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                        popExitTransition = { fadeOut(animationSpec = tween(110)) }
                    ) { entry ->
                        val route = entry.toRoute<ViewerRoute>()
                        PhotoViewerScreen(
                            photos = galleryViewModel.currentDisplayedPhotos(),
                            initialPhotoId = route.photoId,
                            viewModel = galleryViewModel,
                            onClose = { navController.popBackStack() }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !isViewerRoute,
                    enter = fadeIn(tween(140)) + slideInVertically { it },
                    exit = fadeOut(tween(100)) + slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    LiquidGlassPillNavBar(
                        isGallerySelected = isGallerySelected,
                        onGalleryClick = { isGallerySelected = true },
                        onSettingsClick = { isGallerySelected = false }
                    )
                }
            }
        }
    }
}
