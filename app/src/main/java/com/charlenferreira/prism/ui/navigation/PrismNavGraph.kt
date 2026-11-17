package com.charlenferreira.prism.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
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
    var backdropTick by remember { mutableIntStateOf(0) }
    val sharedBackdropState = rememberPrismBackdropState { backdropTick }

    LaunchedEffect(isGallerySelected) {
        backdropTick++
    }

    val fastSpringFloat = spring<Float>(
        dampingRatio = 0.86f,
        stiffness = 480f
    )
    val fastSpringOffset = spring<IntOffset>(
        dampingRatio = 0.84f,
        stiffness = 460f
    )

    CompositionLocalProvider(LocalPrismBackdrop provides sharedBackdropState) {
        LiquidGlassBackground(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = HomeTabsRoute,
                    enterTransition = {
                        fadeIn(animationSpec = tween(160)) +
                            scaleIn(initialScale = 0.96f, animationSpec = fastSpringFloat)
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(120))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(160))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(120)) +
                            scaleOut(targetScale = 0.96f, animationSpec = fastSpringFloat)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable<HomeTabsRoute> {
                        BackHandler(enabled = !isGallerySelected) {
                            isGallerySelected = true
                        }

                        MainTabsHost(
                            isGallerySelected = isGallerySelected,
                            galleryViewModel = galleryViewModel,
                            settingsViewModel = settingsViewModel,
                            sharedBackdropState = sharedBackdropState,
                            onScrollTick = { backdropTick++ },
                            onPhotoClick = { photoId ->
                                navController.navigate(ViewerRoute(photoId = photoId))
                            }
                        )
                    }

                    composable<ViewerRoute>(
                        enterTransition = {
                            fadeIn(animationSpec = tween(180)) +
                                scaleIn(initialScale = 0.92f, animationSpec = fastSpringFloat)
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(130))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(180))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(130)) +
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
                    exit = fadeOut(tween(120)) + slideOutVertically(fastSpringOffset) { it },
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

@Composable
private fun MainTabsHost(
    isGallerySelected: Boolean,
    galleryViewModel: GalleryViewModel,
    settingsViewModel: SettingsViewModel,
    sharedBackdropState: com.charlenferreira.prism.ui.components.PrismBackdropState,
    onScrollTick: () -> Unit,
    onPhotoClick: (Long) -> Unit
) {
    val tabSpring = spring<Float>(dampingRatio = 0.88f, stiffness = 480f)
    val settingsAlpha by animateFloatAsState(
        targetValue = if (!isGallerySelected) 1f else 0f,
        animationSpec = tween(170),
        label = "settingsTabAlpha"
    )
    val settingsScale by animateFloatAsState(
        targetValue = if (!isGallerySelected) 1f else 0.97f,
        animationSpec = tabSpring,
        label = "settingsTabScale"
    )
    val galleryScale by animateFloatAsState(
        targetValue = if (isGallerySelected) 1f else 0.97f,
        animationSpec = tabSpring,
        label = "galleryTabScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Keep GalleryScreen mounted in memory for instant 0ms 120fps tab switches
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = galleryScale
                    scaleY = galleryScale
                    alpha = 1f - settingsAlpha
                }
        ) {
            GalleryScreen(
                viewModel = galleryViewModel,
                onPhotoClick = onPhotoClick,
                sharedBackdropState = if (isGallerySelected) sharedBackdropState else null,
                onScrollTick = {
                    if (isGallerySelected) onScrollTick()
                }
            )
        }

        if (!isGallerySelected || settingsAlpha > 0.01f) {
            val touchBlockerSource = remember { MutableInteractionSource() }
            LiquidGlassBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = settingsAlpha
                        scaleX = settingsScale
                        scaleY = settingsScale
                    }
                    .clickable(
                        interactionSource = touchBlockerSource,
                        indication = null,
                        enabled = !isGallerySelected,
                        onClick = {}
                    )
            ) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    sharedBackdropState = if (!isGallerySelected) sharedBackdropState else null,
                    onScrollTick = {
                        if (!isGallerySelected) onScrollTick()
                    }
                )
            }
        }
    }
}
