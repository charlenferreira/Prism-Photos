package com.charlenferreira.prism.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.charlenferreira.prism.ui.components.LiquidGlassBackground
import com.charlenferreira.prism.ui.components.LiquidGlassPillNavBar
import com.charlenferreira.prism.ui.gallery.GalleryScreen
import com.charlenferreira.prism.ui.gallery.GalleryViewModel
import com.charlenferreira.prism.ui.gallery.PhotoViewerScreen
import com.charlenferreira.prism.ui.settings.SettingsScreen
import com.charlenferreira.prism.ui.settings.SettingsViewModel
import kotlinx.serialization.Serializable

@Serializable
data object GalleryRoute

@Serializable
data object SettingsRoute

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
    val isGallerySelected = currentDestination?.hasRoute<SettingsRoute>() != true

    val iosSpringFloat = spring<Float>(
        dampingRatio = 0.78f,
        stiffness = 460f
    )
    val iosSpringOffset = spring<IntOffset>(
        dampingRatio = 0.78f,
        stiffness = 460f
    )

    LiquidGlassBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = GalleryRoute,
                enterTransition = {
                    fadeIn(animationSpec = iosSpringFloat) +
                        slideInHorizontally(animationSpec = iosSpringOffset) { it / 12 }
                },
                exitTransition = {
                    fadeOut(animationSpec = iosSpringFloat) +
                        slideOutHorizontally(animationSpec = iosSpringOffset) { -it / 16 }
                },
                popEnterTransition = {
                    fadeIn(animationSpec = iosSpringFloat) +
                        slideInHorizontally(animationSpec = iosSpringOffset) { -it / 16 }
                },
                popExitTransition = {
                    fadeOut(animationSpec = iosSpringFloat) +
                        slideOutHorizontally(animationSpec = iosSpringOffset) { it / 12 }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                composable<GalleryRoute> {
                    GalleryScreen(
                        viewModel = galleryViewModel,
                        onPhotoClick = { photoId ->
                            navController.navigate(ViewerRoute(photoId = photoId))
                        }
                    )
                }

                composable<SettingsRoute> {
                    SettingsScreen(viewModel = settingsViewModel)
                }

                composable<ViewerRoute>(
                    enterTransition = {
                        fadeIn(animationSpec = iosSpringFloat) +
                            scaleIn(initialScale = 0.86f, animationSpec = iosSpringFloat)
                    },
                    exitTransition = {
                        fadeOut(animationSpec = iosSpringFloat)
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = iosSpringFloat)
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = iosSpringFloat) +
                            scaleOut(targetScale = 0.86f, animationSpec = iosSpringFloat)
                    }
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
                enter = fadeIn(iosSpringFloat) + slideInVertically(iosSpringOffset) { it },
                exit = fadeOut(iosSpringFloat) + slideOutVertically(iosSpringOffset) { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                LiquidGlassPillNavBar(
                    isGallerySelected = isGallerySelected,
                    onGalleryClick = {
                        navController.navigate(GalleryRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSettingsClick = {
                        navController.navigate(SettingsRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
