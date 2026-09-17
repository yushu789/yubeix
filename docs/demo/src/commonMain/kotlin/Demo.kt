// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.ButtonDefaults
import site.unclefish.yubeix.basic.TextButton
import site.unclefish.yubeix.navigation.NavigationPath
import site.unclefish.yubeix.navigation.SceneDisplay
import site.unclefish.yubeix.navigation.entryProvider
import site.unclefish.yubeix.theme.ColorSchemeMode
import site.unclefish.yubeix.theme.YubeixTheme
import site.unclefish.yubeix.theme.ThemeController

@Composable
fun Demo(demoId: String? = null) {
    val controller = remember { ThemeController(ColorSchemeMode.System) }
    YubeixTheme(controller = controller) {
        if (demoId == null) {
            DemoSelection()
        } else {
            availableComponents.first { it.id == demoId }.demo()
        }
    }
}

private data class AvailableComponent(val name: String, val id: String, val demo: @Composable () -> Unit)

private val availableComponents = listOf(
    AvailableComponent("Scaffold", "scaffold") { ScaffoldDemo() },
    AvailableComponent("Surface", "surface") { SurfaceDemo() },
    AvailableComponent("TopAppBar", "topAppBar") { TopAppBarDemo() },
    AvailableComponent("NavigationBar", "navigationBar") { NavigationBarDemo() },
    AvailableComponent("NavigationRail", "navigationRail") { NavigationRailDemo() },
    AvailableComponent("TabRow", "tabRow") { TabRowDemo() },
    AvailableComponent("Card", "card") { CardDemo() },
    AvailableComponent("BasicComponent", "basicComponent") { BasicComponentDemo() },
    AvailableComponent("Button", "button") { ButtonDemo() },
    AvailableComponent("IconButton", "iconButton") { IconButtonDemo() },
    AvailableComponent("Text", "text") { TextDemo() },
    AvailableComponent("SmallTitle", "smallTitle") { SmallTitleDemo() },
    AvailableComponent("TextField", "textField") { TextFieldDemo() },
    AvailableComponent("Switch", "switch") { SwitchDemo() },
    AvailableComponent("Checkbox", "checkbox") { CheckboxDemo() },
    AvailableComponent("RadioButton", "radioButton") { RadioButtonDemo() },
    AvailableComponent("Slider", "slider") { SliderDemo() },
    AvailableComponent("NumberPicker", "numberPicker") { NumberPickerDemo() },
    AvailableComponent("ProgressIndicator", "progressIndicator") { ProgressIndicatorDemo() },
    AvailableComponent("Snackbar", "snackbar") { SnackbarDemo() },
    AvailableComponent("Icon", "icon") { IconDemo() },
    AvailableComponent("FloatingActionButton", "floatingActionButton") { FloatingActionButtonDemo() },
    AvailableComponent("FloatingToolbar", "floatingToolbar") { FloatingToolbarDemo() },
    AvailableComponent("Divider", "divider") { DividerDemo() },
    AvailableComponent("PullToRefresh", "pullToRefresh") { PullToRefreshDemo() },
    AvailableComponent("SearchBar", "searchBar") { SearchBarDemo() },
    AvailableComponent("ColorPicker", "colorPicker") { ColorPickerDemo() },
    AvailableComponent("ColorPalette", "colorPalette") { ColorPaletteDemo() },
    AvailableComponent("SuperArrow", "superArrow") { SuperArrowDemo() },
    AvailableComponent("SuperSwitch", "superSwitch") { SuperSwitchDemo() },
    AvailableComponent("SuperCheckbox", "superCheckbox") { SuperCheckboxDemo() },
    AvailableComponent("SuperRadioButton", "superRadioButton") { SuperRadioButtonDemo() },
    AvailableComponent("SuperListPopup", "superListPopup") { SuperListPopupDemo() },
    AvailableComponent("SuperDropdown", "superDropdown") { SuperDropdownDemo() },
    AvailableComponent("SuperSpinner", "superSpinner") { SuperSpinnerDemo() },
    AvailableComponent("SuperBottomSheet", "superBottomSheet") { SuperBottomSheetDemo() },
    AvailableComponent("SuperDialog", "superDialog") { SuperDialogDemo() },
    AvailableComponent("WindowListPopup", "windowListPopup") { WindowListPopupDemo() },
    AvailableComponent("WindowDropdown", "windowDropdown") { WindowDropdownDemo() },
    AvailableComponent("WindowSpinner", "windowSpinner") { WindowSpinnerDemo() },
    AvailableComponent("WindowBottomSheet", "windowBottomSheet") { WindowBottomSheetDemo() },
    AvailableComponent("WindowDialog", "windowDialog") { WindowDialogDemo() },
)

@Composable
private fun DemoSelection() {
    val navigationPath = remember { NavigationPath<DemoScreen>(DemoScreen.Home) }
    val entryProvider = remember {
        entryProvider<DemoScreen> {
            entry(DemoScreen.Home) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(YubeixTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                            .widthIn(max = 600.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        availableComponents.forEach { demo ->
                            TextButton(
                                text = demo.name,
                                onClick = { navigationPath.push(DemoScreen.Component(demo.id)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        }
                    }
                }
            }

            availableComponents.forEach { component ->
                entry(DemoScreen.Component(component.id)) {
                    Column {
                        component.demo()
                    }
                }
            }
        }
    }

    SceneDisplay(
        navigationPath = navigationPath,
        entryProvider = entryProvider,
        predictiveBackEnabled = true,
    )
}

private sealed interface DemoScreen : site.unclefish.yubeix.navigation.NavKey {
    data object Home : DemoScreen
    data class Component(val id: String) : DemoScreen
}
