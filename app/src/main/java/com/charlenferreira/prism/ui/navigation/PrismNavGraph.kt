package com.charlenferreira.prism.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
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

    val springFloatSpec = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = 800f
    )
    val springOffsetSpec = spring<IntOffset>(
        dampingRatio = 0.6f,
        stiffness = 800f
    )

    LiquidGlassBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = GalleryRoute,
                enterTransition = {
                    fadeIn(animationSpec = springFloatSpec) +
                        scaleIn(initialScale = 0.94f, animationSpec = springFloatSpec)
                },
                exitTransition = {
                    fadeOut(animationSpec = springFloatSpec) +
                        scaleOut(targetScale = 0.96f, animationSpec = springFloatSpec)
                },
                popEnterTransition = {
                    fadeIn(animationSpec = springFloatSpec) +
                        scaleIn(initialScale = 0.96f, animationSpec = springFloatSpec)
                },
                popExitTransition = {
                    fadeOut(animationSpec = springFloatSpec) +
                        scaleOut(targetScale = 0.94f, animationSpec = springFloatSpec)
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
                        fadeIn(animationSpec = springFloatSpec) +
                            scaleIn(initialScale = 0.88f, animationSpec = springFloatSpec)
                    },
                    exitTransition = {
                        fadeOut(animationSpec = springFloatSpec)
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = springFloatSpec)
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = springFloatSpec) +
                            scaleOut(targetScale = 0.88f, animationSpec = springFloatSpec)
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
                enter = fadeIn(springFloatSpec) + slideInVertically(springOffsetSpec) { it },
                exit = fadeOut(springFloatSpec) + slideOutVertically(springOffsetSpec) { it },
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
