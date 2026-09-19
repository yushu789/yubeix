// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.rememberHazeState
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.CardDefaults
import site.unclefish.yubeix.basic.ScreenScaffold
import site.unclefish.yubeix.basic.ScreenTitleMode
import site.unclefish.yubeix.basic.SmallTitle
import site.unclefish.yubeix.basic.Surface
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.theme.Colors
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.darkColorScheme
import site.unclefish.yubeix.theme.lightColorScheme
import site.unclefish.yubeix.theme.platformDynamicColors

private val CamelCaseRegex = Regex("([A-Z])")
private val ColorBlockShape = RoundedCornerShape(12.dp)

@Immutable
private data class ColorBlockData(
    val name: String,
    val displayName: String,
    val surfaceColor: Color,
    val textColor: Color,
)

@Composable
fun ColorPage(
    padding: PaddingValues,
) {
    val isWideScreen = LocalIsWideScreen.current
    val lazyListState = rememberLazyListState()
    // ScreenScaffold marks the scrolling content as the haze source itself, so the chrome bar can
    // frost it without a manual hazeSource on the list.
    val hazeState = rememberHazeState()
    // The compact shell's bottom bar floats over the page, so its height joins the content's
    // bottom padding; the wide pane has no bottom bar and the scaffold's own inset is enough.
    val bottomBarOverlayHeight = if (isWideScreen) 0.dp else padding.calculateBottomPadding()
    // Keeps the scrollbar track between the chrome bar at the top and the page bottom.
    val scrollBarTrackPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = if (isWideScreen) {
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        } else {
            bottomBarOverlayHeight
        },
    )

    val lightColors = remember { lightColorScheme() }
    val darkColors = remember { darkColorScheme() }
    val dynLight = platformDynamicColors(dark = false)
    val dynDark = platformDynamicColors(dark = true)

    ScreenScaffold(
        title = "Color",
        onBack = null,
        titleMode = ScreenTitleMode.Hero,
        listState = lazyListState,
        hazeState = hazeState,
        itemSpacing = 0.dp,
        bottomContentPadding = 32.dp + bottomBarOverlayHeight,
        floatingBottomContent = {
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(lazyListState),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                trackPadding = scrollBarTrackPadding,
            )
        },
    ) {
        item(key = "current") {
            SmallTitle("Current Theme Colors")
            Card(
                modifier = Modifier.padding(bottom = 12.dp),
                colors = CardDefaults.defaultColors(color = YubeixTheme.colorScheme.surfaceContainer),
                cornerRadius = 16.dp,
                insideMargin = PaddingValues(horizontal = 16.dp),
            ) {
                ColorsPreview(YubeixTheme.colorScheme)
            }
        }
        item(key = "light") {
            SmallTitle("Light Theme Colors")
            Card(
                modifier = Modifier.padding(bottom = 12.dp),
                colors = CardDefaults.defaultColors(color = lightColors.surfaceContainer),
                cornerRadius = 16.dp,
                insideMargin = PaddingValues(horizontal = 16.dp),
            ) {
                ColorsPreview(lightColors)
            }
        }
        item(key = "dynamic_light") {
            SmallTitle("Dynamic Light Colors")
            Card(
                modifier = Modifier.padding(bottom = 12.dp),
                colors = CardDefaults.defaultColors(color = dynLight.surfaceContainer),
                cornerRadius = 16.dp,
                insideMargin = PaddingValues(horizontal = 16.dp),
            ) {
                ColorsPreview(dynLight)
            }
        }
        item(key = "dark") {
            SmallTitle("Dark Theme Colors")
            Card(
                modifier = Modifier.padding(bottom = 12.dp),
                colors = CardDefaults.defaultColors(color = darkColors.surfaceContainer),
                cornerRadius = 16.dp,
                insideMargin = PaddingValues(horizontal = 16.dp),
            ) {
                ColorsPreview(darkColors)
            }
        }
        item(key = "dynamic_dark") {
            SmallTitle("Dynamic Dark Colors")
            Card(
                modifier = Modifier,
                colors = CardDefaults.defaultColors(color = dynDark.surfaceContainer),
                cornerRadius = 16.dp,
                insideMargin = PaddingValues(horizontal = 16.dp),
            ) {
                ColorsPreview(dynDark)
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
fun ColorsPreview(colors: Colors) {
    val colorBlocks = remember(colors) {
        val colorList = listOf(
            "primary" to colors.primary,
            "onPrimary" to colors.onPrimary,
            "primaryVariant" to colors.primaryVariant,
            "onPrimaryVariant" to colors.onPrimaryVariant,
            "error" to colors.error,
            "onError" to colors.onError,
            "errorContainer" to colors.errorContainer,
            "onErrorContainer" to colors.onErrorContainer,
            "disabledPrimary" to colors.disabledPrimary,
            "disabledOnPrimary" to colors.disabledOnPrimary,
            "disabledPrimaryButton" to colors.disabledPrimaryButton,
            "disabledOnPrimaryButton" to colors.disabledOnPrimaryButton,
            "disabledPrimarySlider" to colors.disabledPrimarySlider,
            "primaryContainer" to colors.primaryContainer,
            "onPrimaryContainer" to colors.onPrimaryContainer,
            "secondary" to colors.secondary,
            "onSecondary" to colors.onSecondary,
            "secondaryVariant" to colors.secondaryVariant,
            "onSecondaryVariant" to colors.onSecondaryVariant,
            "disabledSecondary" to colors.disabledSecondary,
            "disabledOnSecondary" to colors.disabledOnSecondary,
            "disabledSecondaryVariant" to colors.disabledSecondaryVariant,
            "disabledOnSecondaryVariant" to colors.disabledOnSecondaryVariant,
            "secondaryContainer" to colors.secondaryContainer,
            "onSecondaryContainer" to colors.onSecondaryContainer,
            "secondaryContainerVariant" to colors.secondaryContainerVariant,
            "onSecondaryContainerVariant" to colors.onSecondaryContainerVariant,
            "tertiaryContainer" to colors.tertiaryContainer,
            "onTertiaryContainer" to colors.onTertiaryContainer,
            "tertiaryContainerVariant" to colors.tertiaryContainerVariant,
            "background" to colors.background,
            "onBackground" to colors.onBackground,
            "onBackgroundVariant" to colors.onBackgroundVariant,
            "surface" to colors.surface,
            "onSurface" to colors.onSurface,
            "surfaceVariant" to colors.surfaceVariant,
            "onSurfaceSecondary" to colors.onSurfaceSecondary,
            "onSurfaceVariantSummary" to colors.onSurfaceVariantSummary,
            "onSurfaceVariantActions" to colors.onSurfaceVariantActions,
            "disabledOnSurface" to colors.disabledOnSurface,
            "surfaceContainer" to colors.surfaceContainer,
            "onSurfaceContainer" to colors.onSurfaceContainer,
            "onSurfaceContainerVariant" to colors.onSurfaceContainerVariant,
            "surfaceContainerHigh" to colors.surfaceContainerHigh,
            "onSurfaceContainerHigh" to colors.onSurfaceContainerHigh,
            "surfaceContainerHighest" to colors.surfaceContainerHighest,
            "onSurfaceContainerHighest" to colors.onSurfaceContainerHighest,
            "outline" to colors.outline,
            "dividerLine" to colors.dividerLine,
            "windowDimming" to colors.windowDimming,
            "sliderKeyPoint" to colors.sliderKeyPoint,
            "sliderKeyPointForeground" to colors.sliderKeyPointForeground,
        )
        val colorMap = colorList.toMap()
        colorList.map { (name, color) ->
            val (surfaceColor, textColor) = when {
                name.startsWith("on") && colorMap.containsKey(name.removePrefix("on").replaceFirstChar { it.lowercase() }) -> {
                    color to colorMap[name.removePrefix("on").replaceFirstChar { it.lowercase() }]!!
                }

                !name.startsWith("on") && colorMap.containsKey("on" + name.replaceFirstChar { it.uppercase() }) -> {
                    color to colorMap["on" + name.replaceFirstChar { it.uppercase() }]!!
                }

                else -> color to (if (color.luminance() > 0.5) Color.Black else Color.White)
            }
            val displayName = name.replace(CamelCaseRegex, " $1").trim().replaceFirstChar { it.uppercase() }
            ColorBlockData(name, displayName, surfaceColor, textColor)
        }
    }

    Column(
        modifier = Modifier.padding(top = 16.dp),
    ) {
        colorBlocks.chunked(2).forEach { rowColors ->
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                rowColors.forEachIndexed { idx, block ->
                    ColorBlock(
                        displayName = block.displayName,
                        surfaceColor = block.surfaceColor,
                        textColor = block.textColor,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (idx < rowColors.lastIndex) Modifier.padding(end = 16.dp) else Modifier),
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorBlock(displayName: String, surfaceColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = surfaceColor,
        shape = ColorBlockShape,
        border = BorderStroke(1.dp, textColor),
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayName,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
    }
}
