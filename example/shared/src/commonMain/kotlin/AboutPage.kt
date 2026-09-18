// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appnavigation.Route
import misc.VersionInfo
import org.jetbrains.compose.resources.painterResource
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.ScreenScaffold
import site.unclefish.yubeix.basic.ScreenTitleMode
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.shared.generated.resources.Res
import site.unclefish.yubeix.shared.generated.resources.ic_launcher
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.YubeixTheme.colorScheme

@Composable
fun AboutPage(
    padding: PaddingValues,
) {
    val isWideScreen = LocalIsWideScreen.current
    val uriHandler = LocalUriHandler.current
    val navigator = LocalNavigator.current
    val lazyListState = rememberLazyListState()
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

    ScreenScaffold(
        title = "About",
        onBack = { navigator.pop() },
        titleMode = ScreenTitleMode.Pinned,
        listState = lazyListState,
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
        item(key = "about") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp, bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White),
                ) {
                    Image(
                        modifier = Modifier.size(80.dp),
                        painter = painterResource(Res.drawable.ic_launcher),
                        contentDescription = null,
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = "Yubeix",
                    fontWeight = FontWeight.Medium,
                    fontSize = 26.sp,
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "v" + VersionInfo.VERSION_NAME + " (" + VersionInfo.VERSION_CODE + ")",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Card(
                modifier = Modifier,
            ) {
                SuperArrow(
                    title = "View Source",
                    endActions = {
                        Text(
                            text = "GitHub",
                            fontSize = YubeixTheme.textStyles.body2.fontSize,
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    onClick = { uriHandler.openUri("https://github.com/compose-miuix-ui/miuix") },
                )
                SuperArrow(
                    title = "Join Group",
                    endActions = {
                        Text(
                            text = "Telegram",
                            fontSize = YubeixTheme.textStyles.body2.fontSize,
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    onClick = { uriHandler.openUri("https://t.me/YuKongA13579") },
                )
            }
            Card(
                modifier = Modifier
                    .padding(top = 12.dp),
            ) {
                SuperArrow(
                    title = "License",
                    endActions = {
                        Text(
                            text = "Apache-2.0",
                            fontSize = YubeixTheme.textStyles.body2.fontSize,
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    onClick = {
                        uriHandler.openUri("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    },
                )
                SuperArrow(
                    title = "Third Party Licenses",
                    onClick = { navigator.push(Route.License) },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
