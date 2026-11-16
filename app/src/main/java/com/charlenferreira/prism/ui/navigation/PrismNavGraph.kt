package com.charlenferreira.prism.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val routeIsGallery = currentDestination?.hasRoute<SettingsRoute>() != true

    // Optimistic selection updates on frame 0 of tap without waiting for NavController backstack emit
    var optimisticGallerySelected by remember { mutableStateOf(true) }
    LaunchedEffect(routeIsGallery) {
        optimisticGallerySelected = routeIsGallery
    }

    val fastSpringFloat = spring<Float>(
        dampingRatio = 0.84f,
        stiffness = 500f
    )
    val fastSpringOffset = spring<IntOffset>(
        dampingRatio = 0.82f,
        stiffness = 480f
    )

    LiquidGlassBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = GalleryRoute,
                enterTransition = {
                    fadeIn(animationSpec = tween(160)) +
                        scaleIn(initialScale = 0.97f, animationSpec = fastSpringFloat)
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(120))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(160))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(120)) +
                        scaleOut(targetScale = 0.97f, animationSpec = fastSpringFloat)
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
                        fadeIn(animationSpec = tween(180)) +
                            scaleIn(initialScale = 0.92f, animationSpec = fastSpringFloat)
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(140))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(180))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(140)) +
                            scaleOut(targetScale = 0.92f, animationSpec = fastSpringFloat)
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
                enter = fadeIn(tween(160)) + slideInVertically(fastSpringOffset) { it },
                exit = fadeOut(tween(130)) + slideOutVertically(fastSpringOffset) { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                LiquidGlassPillNavBar(
                    isGallerySelected = optimisticGallerySelected,
                    onGalleryClick = {
                        if (!optimisticGallerySelected) {
                            optimisticGallerySelected = true
                            navController.navigate(GalleryRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onSettingsClick = {
                        if (optimisticGallerySelected) {
                            optimisticGallerySelected = false
                            navController.navigate(SettingsRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}
