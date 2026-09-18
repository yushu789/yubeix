// Copyright 2025, yubeix contributors
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
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import appnavigation.Route
import component.BackNavigationIcon
import misc.VersionInfo
import org.jetbrains.compose.resources.painterResource
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.Scaffold
import site.unclefish.yubeix.basic.ScrollBehavior
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.YubeixScrollBehavior
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.extra.SuperArrow
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.shared.generated.resources.Res
import site.unclefish.yubeix.shared.generated.resources.ic_launcher
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.YubeixTheme.colorScheme
import utils.AdaptiveTopAppBar
import utils.pageContentPadding
import utils.pageScrollModifiers

@Composable
fun AboutPage(
    padding: PaddingValues,
) {
    val appState = LocalAppState.current
    val isWideScreen = LocalIsWideScreen.current
    val topAppBarScrollBehavior = YubeixScrollBehavior()
    val navigator = LocalNavigator.current
    Scaffold(
        topBar = {
            AdaptiveTopAppBar(
                title = "About",
                showTopAppBar = appState.showTopAppBar,
                isWideScreen = isWideScreen,
                scrollBehavior = topAppBarScrollBehavior,
                navigationIcon = {
                    BackNavigationIcon(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = { navigator.pop() },
                    )
                },
            )
        },
    ) { innerPadding ->
        AboutContent(
            padding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            topAppBarScrollBehavior = topAppBarScrollBehavior,
        )
    }
}

@Composable
private fun AboutContent(
    padding: PaddingValues,
    topAppBarScrollBehavior: ScrollBehavior,
) {
    val appState = LocalAppState.current
    val isWideScreen = LocalIsWideScreen.current
    val uriHandler = LocalUriHandler.current
    val navigator = LocalNavigator.current
    val lazyListState = rememberLazyListState()

    val contentPadding = pageContentPadding(
        padding,
        padding,
        isWideScreen,
        extraStart = WindowInsets.displayCutout.asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr),
        extraEnd = WindowInsets.displayCutout.asPaddingValues().calculateRightPadding(LayoutDirection.Ltr),
    )
    Box {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.pageScrollModifiers(
                appState.enableScrollEndHaptic,
                appState.showTopAppBar,
                topAppBarScrollBehavior,
            ),
            contentPadding = contentPadding,
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
        VerticalScrollBar(
            adapter = rememberScrollBarAdapter(lazyListState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            trackPadding = contentPadding,
        )
    }
}
