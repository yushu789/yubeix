// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalScrollBarApi::class)

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.arrowSection
import component.basicComponentSection
import component.bottomSheetSection
import component.buttonSection
import component.cardSection
import component.checkboxSection
import component.colorPickerSection
import component.dialogSection
import component.dropdownSection
import component.numberPickerSection
import component.progressIndicatorSection
import component.radioButtonSection
import component.sliderSection
import component.snackbarSection
import component.spinnerSection
import component.switchSection
import component.tabRowSection
import component.textFieldSection
import dev.chrisbanes.haze.rememberHazeState
import site.unclefish.yubeix.basic.BasicComponent
import site.unclefish.yubeix.basic.InputField
import site.unclefish.yubeix.basic.ScreenScaffold
import site.unclefish.yubeix.basic.ScreenTitleMode
import site.unclefish.yubeix.basic.SearchBar
import site.unclefish.yubeix.basic.SmallTitle
import site.unclefish.yubeix.basic.SnackbarHostState
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.basic.VerticalScrollBar
import site.unclefish.yubeix.basic.rememberScrollBarAdapter
import site.unclefish.yubeix.interfaces.ExperimentalScrollBarApi
import site.unclefish.yubeix.theme.YubeixTheme

@Composable
fun MainPage(
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues,
) {
    var searchValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val notExpanded by remember { derivedStateOf { !expanded } }
    val onCancelSearch = remember {
        {
            expanded = false
            searchValue = ""
        }
    }

    val isWideScreen = LocalIsWideScreen.current
    val lazyListState = rememberLazyListState()
    // The chrome bar frosts the scrolling content through this state; ScreenScaffold marks the
    // content as the haze source itself, so the list needs no manual hazeSource here.
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

    ScreenScaffold(
        title = "Home",
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
        item(key = "searchbar") {
            SmallTitle(text = "SearchBar")
            SearchBar(
                modifier = Modifier.padding(bottom = 12.dp),
                inputField = {
                    InputField(
                        query = searchValue,
                        onQueryChange = { searchValue = it },
                        onSearch = { expanded = false },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        label = "Search",
                    )
                },
                outsideEndAction = {
                    Text(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = onCancelSearch,
                            ),
                        text = "Cancel",
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        color = YubeixTheme.colorScheme.primary,
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                Column {
                    repeat(4) { idx ->
                        val resultText = "Suggestion $idx"
                        BasicComponent(
                            title = resultText,
                            onClick = {
                                searchValue = resultText
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
        if (notExpanded) {
            basicComponentSection()
            checkboxSection()
            radioButtonSection()
            switchSection()
            arrowSection()
            dialogSection()
            bottomSheetSection()
            dropdownSection()
            spinnerSection()
            buttonSection()
            snackbarSection(snackbarHostState)
            progressIndicatorSection()
            textFieldSection()
            sliderSection()
            tabRowSection()
            numberPickerSection()
            colorPickerSection()
            cardSection()
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
