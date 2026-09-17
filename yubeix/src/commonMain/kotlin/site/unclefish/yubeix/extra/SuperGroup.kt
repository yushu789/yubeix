// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.extra

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.CardColors
import site.unclefish.yubeix.basic.CardDefaults
import site.unclefish.yubeix.basic.HorizontalDivider
import site.unclefish.yubeix.theme.YubeixTheme

/**
 * ColumnScope handed to [SuperGroup] content. The rows are measured by a SubcomposeLayout rather
 * than a real Column, so the scope's placement modifiers are no-ops here - the same behavior the
 * rows would get from any custom layout host.
 */
private object SuperGroupColumnScope : ColumnScope {
    override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = this
    override fun Modifier.align(alignment: Alignment.Horizontal): Modifier = this
    override fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine): Modifier = this
    override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier = this
}

/**
 * A grouped preference container, equivalent to wordmoment's preference group card. It renders
 * its content inside a rounded [Card] and draws a divider between every two adjacent visible
 * children.
 *
 * The rows inside the group handle their own padding, so [insideMargin] is not applied around the
 * content; it is used to inset the dividers from the start edge, mirroring the source behavior.
 * Invisible children (zero width or height) do not produce dividers.
 *
 * @param modifier The modifier to be applied to the [SuperGroup].
 * @param cornerRadius The corner radius of the group card.
 * @param insideMargin The margin used to inset the dividers from the start edge of the group.
 * @param colors The [CardColors] of the group card.
 * @param dividerStartInset The extra inset added to the start padding of the dividers.
 * @param content The preference rows of the group.
 */
@Composable
fun SuperGroup(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = SuperRowDefaults.CardCornerRadius,
    insideMargin: PaddingValues = SuperRowDefaults.ItemPadding,
    colors: CardColors = CardDefaults.defaultColors(),
    dividerStartInset: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val dividerPadding = PaddingValues(
        start = insideMargin.calculateStartPadding(layoutDirection) + dividerStartInset,
        end = 0.dp,
    )

    Card(
        modifier = modifier,
        cornerRadius = cornerRadius,
        insideMargin = PaddingValues(0.dp),
        colors = colors,
    ) {
        SubcomposeLayout { constraints ->
            val contentPlaceables = subcompose("content") {
                // The rows are measured by this SubcomposeLayout, not laid out by a Column, so
                // invoke the content under an explicit ColumnScope receiver instead of relying
                // on the enclosing Card lambda's scope.
                SuperGroupColumnScope.content()
            }.map { measurable ->
                measurable.measure(constraints.copy(minHeight = 0))
            }
            val visiblePlaceables = contentPlaceables.filter { it.width > 0 && it.height > 0 }
            val dividerPlaceables = if (visiblePlaceables.size > 1) {
                subcompose("dividers") {
                    repeat(visiblePlaceables.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dividerPadding),
                            color = YubeixTheme.colorScheme.dividerLine,
                        )
                    }
                }.map { measurable -> measurable.measure(constraints.copy(minHeight = 0)) }
            } else {
                emptyList()
            }
            val width = (contentPlaceables.maxOfOrNull { it.width } ?: 0)
                .coerceAtLeast(dividerPlaceables.maxOfOrNull { it.width } ?: 0)
                .coerceIn(constraints.minWidth, constraints.maxWidth)
            val height = contentPlaceables.sumOf { it.height }
                .coerceIn(constraints.minHeight, constraints.maxHeight)

            layout(width, height) {
                var y = 0
                var visibleIndex = 0
                contentPlaceables.forEach { placeable ->
                    placeable.placeRelative(0, y)
                    y += placeable.height
                    if (placeable.width > 0 && placeable.height > 0) {
                        dividerPlaceables.getOrNull(visibleIndex)?.placeRelative(0, y)
                        visibleIndex += 1
                    }
                }
            }
        }
    }
}
