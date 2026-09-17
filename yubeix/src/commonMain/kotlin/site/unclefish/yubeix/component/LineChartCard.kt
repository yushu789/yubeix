// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import site.unclefish.yubeix.basic.Card
import site.unclefish.yubeix.basic.CardDefaults
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.theme.YubeixTheme
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

private val ChartCardPadding = 16.dp
private val ChartPlotHeight = 150.dp
private val ChartColumnWidth = 64.dp
private val ChartAxisWidth = 36.dp
private val ChartAxisLabelWidth = 30.dp
private val ChartPlotPadding = 2.dp
private val ChartRightPadding = 24.dp
private val ChartTopPadding = 26.dp
private val ChartBottomPadding = 24.dp
private val ChartBarWidth = 9.dp
private val ChartBarGap = 2.dp
private val ChartBarRadius = 3.dp
private val ChartLineWidth = 2.6.dp
private val ChartPointRadius = 5.dp
private val ChartInnerPointRadius = 2.dp
private const val CHART_AXIS_STEP_COUNT = 4

/**
 * One labeled data point along a chart's x axis.
 *
 * @param label The caption drawn under the point's column.
 * @param value The plotted amount; rendered against the card's auto-scaled y axis.
 */
data class ChartPoint(
    val label: String,
    val value: Float,
)

/**
 * A named, colored set of values for [BarChartCard]: one bar per index inside each x-axis column,
 * and one legend entry. [values] is read position against [BarChartCard.pointLabels]; extra values
 * beyond the label count are ignored.
 */
data class ChartSeries(
    val label: String,
    val color: Color,
    val values: List<Float>,
)

/** One color swatch plus caption in a chart legend row. */
data class ChartLegendItem(
    val color: Color,
    val label: String,
)

/**
 * A row of small circular swatches with captions, describing the colors used by a chart above it.
 */
@Composable
fun ChartLegendRow(
    items: List<ChartLegendItem>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(item.color, CircleShape),
                )
                Text(
                    text = item.label,
                    style = YubeixTheme.textStyles.footnote1,
                    color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

/**
 * A card with a line chart: a fixed y axis with grid lines, one line with ringed dots per labeled
 * point, optional value captions above the dots, and a legend row. The plot scrolls horizontally
 * when the data has more than five points and auto-scrolls to the newest one.
 *
 * @param title The caption shown at the top of the card, next to [icon].
 * @param points The labeled values to plot, in left-to-right order.
 * @param modifier The modifier applied to the card.
 * @param icon Optional icon drawn before [title].
 * @param unit The y-axis unit caption drawn at the top of the axis.
 * @param color The line, dot and legend color.
 * @param pointValueLabels Whether to draw each point's value above its dot.
 * @param valueFormatter Formats the value captions above the dots.
 * @param axisLabelFormatter Formats the y-axis tick labels; the ticks are chosen as a nice step
 *  from the data's maximum, so format for round values.
 * @param legendLabel Optional caption for a single-item legend row; pass `null` for no legend.
 */
@Composable
fun LineChartCard(
    title: String,
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    unit: String = "",
    color: Color = YubeixTheme.colorScheme.primary,
    pointValueLabels: Boolean = true,
    valueFormatter: (Float) -> String = { it.roundToInt().toString() },
    axisLabelFormatter: (Float) -> String = { it.roundToInt().toString() },
    legendLabel: String? = null,
) {
    val colors = YubeixTheme.colorScheme
    val maxValue = points.maxOfOrNull { it.value } ?: 0f
    val axisStep = remember(maxValue) { computeNiceAxisStep(maxValue) }
    val axisMax = axisStep * CHART_AXIS_STEP_COUNT
    val legendItems = legendLabel?.let { listOf(ChartLegendItem(color = color, label = it)) }
    val legend: (@Composable () -> Unit)? = legendItems?.let { items ->
        { ChartLegendRow(items = items) }
    }

    ChartCardFrame(
        modifier = modifier,
        title = title,
        icon = icon,
        unit = unit,
        axisLabels = List(CHART_AXIS_STEP_COUNT + 1) { index ->
            axisLabelFormatter(axisStep * (CHART_AXIS_STEP_COUNT - index))
        },
        plotCount = points.size,
        legend = legend,
    ) {
        LineChartPlot(
            points = points,
            color = color,
            surfaceColor = colors.surface,
            dividerColor = colors.dividerLine,
            labelColor = colors.onSurfaceVariantSummary,
            axisMax = axisMax,
            showValueLabels = pointValueLabels,
            valueFormatter = valueFormatter,
        )
    }
}

/**
 * A card with a grouped bar chart: a fixed y axis with grid lines, one bar per series in each
 * labeled column with a value caption above it, and a legend row derived from the series. The
 * plot scrolls horizontally when the data has more than five columns and auto-scrolls to the
 * newest one.
 *
 * @param title The caption shown at the top of the card, next to [icon].
 * @param pointLabels The x-axis captions, one per column, in left-to-right order.
 * @param series The bar groups to draw; each series contributes one bar per column.
 * @param modifier The modifier applied to the card.
 * @param icon Optional icon drawn before [title].
 * @param unit The y-axis unit caption drawn at the top of the axis.
 * @param barValueLabels Whether to draw each bar's value above it.
 * @param valueFormatter Formats the value captions above the bars.
 * @param axisLabelFormatter Formats the y-axis tick labels; the ticks are chosen as a nice step
 *  from the data's maximum, so format for round values.
 */
@Composable
fun BarChartCard(
    title: String,
    pointLabels: List<String>,
    series: List<ChartSeries>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    unit: String = "",
    barValueLabels: Boolean = true,
    valueFormatter: (Float) -> String = { it.roundToInt().toString() },
    axisLabelFormatter: (Float) -> String = { it.roundToInt().toString() },
) {
    val colors = YubeixTheme.colorScheme
    val maxValue = series.flatMap { it.values }.maxOfOrNull { it } ?: 0f
    val axisStep = remember(maxValue) { computeNiceAxisStep(maxValue) }
    val axisMax = axisStep * CHART_AXIS_STEP_COUNT
    val legendItems = remember(series) { series.map { ChartLegendItem(it.color, it.label) } }

    ChartCardFrame(
        modifier = modifier,
        title = title,
        icon = icon,
        unit = unit,
        axisLabels = List(CHART_AXIS_STEP_COUNT + 1) { index ->
            axisLabelFormatter(axisStep * (CHART_AXIS_STEP_COUNT - index))
        },
        plotCount = pointLabels.size,
        legend = { ChartLegendRow(items = legendItems) },
    ) {
        BarChartPlot(
            pointLabels = pointLabels,
            series = series,
            surfaceColor = colors.surface,
            dividerColor = colors.dividerLine,
            labelColor = colors.onSurfaceVariantSummary,
            axisMax = axisMax,
            showBarValueLabels = barValueLabels,
            valueFormatter = valueFormatter,
        )
    }
}

/**
 * The shared chart-card scaffold: header, fixed y axis, horizontally scrolling plot area, and an
 * optional legend row.
 */
@Composable
private fun ChartCardFrame(
    title: String,
    icon: ImageVector?,
    unit: String,
    axisLabels: List<String>,
    plotCount: Int,
    modifier: Modifier = Modifier,
    legend: (@Composable () -> Unit)? = null,
    chart: @Composable () -> Unit,
) {
    val colors = YubeixTheme.colorScheme
    val scrollState = rememberScrollState()
    val chartWidth = ChartPlotPadding * 2f +
        ChartColumnWidth * plotCount.coerceAtLeast(5).toFloat()

    LaunchedEffect(plotCount) {
        snapshotFlow { scrollState.maxValue }.first { it > 0 }
        scrollState.scrollTo(scrollState.maxValue)
    }

    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.defaultColors(color = colors.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = ChartCardPadding),
        ) {
            ChartCardHeader(
                modifier = Modifier.padding(horizontal = ChartCardPadding),
                icon = icon,
                title = title,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartPlotHeight)
                    .padding(horizontal = 4.dp),
            ) {
                ChartFixedAxis(
                    unit = unit,
                    labels = axisLabels,
                    modifier = Modifier
                        .width(ChartAxisWidth)
                        .fillMaxHeight(),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(scrollState),
                ) {
                    Box(modifier = Modifier.width(chartWidth)) { chart() }
                }
                Spacer(modifier = Modifier.width(ChartRightPadding))
            }
            if (legend != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.padding(horizontal = ChartCardPadding)) { legend() }
            }
        }
    }
}

@Composable
private fun ChartCardHeader(
    icon: ImageVector?,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = YubeixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = title,
            style = YubeixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Normal),
            color = YubeixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun ChartFixedAxis(
    unit: String,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    val colors = YubeixTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()
    val style = chartAxisTextStyle(colors.onSurfaceVariantSummary)
    Canvas(modifier = modifier) {
        val top = ChartTopPadding.toPx()
        val bottom = size.height - ChartBottomPadding.toPx()
        val height = (bottom - top).coerceAtLeast(1f)
        drawTextAt(
            textMeasurer = textMeasurer,
            text = unit,
            style = style,
            topLeft = Offset(16.dp.toPx(), 0f),
        )
        drawLine(
            color = colors.dividerLine,
            start = Offset(size.width, top),
            end = Offset(size.width, bottom),
            strokeWidth = 1.dp.toPx(),
        )
        labels.forEachIndexed { index, label ->
            val y = top + height * index / CHART_AXIS_STEP_COUNT.toFloat()
            val layout = textMeasurer.measure(AnnotatedString(label), style)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = ChartAxisLabelWidth.toPx() - layout.size.width,
                    y = y - layout.size.height / 2f,
                ),
            )
        }
    }
}

@Composable
private fun LineChartPlot(
    points: List<ChartPoint>,
    color: Color,
    surfaceColor: Color,
    dividerColor: Color,
    labelColor: Color,
    axisMax: Float,
    showValueLabels: Boolean,
    valueFormatter: (Float) -> String,
) {
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = chartAxisTextStyle(labelColor)
    val valueStyle = TextStyle(color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    Canvas(Modifier.fillMaxSize()) {
        val start = ChartPlotPadding.toPx()
        val end = size.width - ChartPlotPadding.toPx()
        val top = ChartTopPadding.toPx()
        val bottom = size.height - ChartBottomPadding.toPx()
        val height = (bottom - top).coerceAtLeast(1f)
        val columnWidth = (end - start).coerceAtLeast(1f) / points.size.coerceAtLeast(1)
        drawChartGrid(start, end, top, bottom, dividerColor)
        if (axisMax > 0f && points.isNotEmpty()) {
            val offsets = points.mapIndexed { index, point ->
                val fraction = (point.value / axisMax).coerceIn(0f, 1f)
                Offset(start + columnWidth * index + columnWidth / 2f, bottom - height * fraction)
            }
            offsets.zipWithNext().forEach { (from, to) ->
                drawLine(color, from, to, ChartLineWidth.toPx(), StrokeCap.Round)
            }
            offsets.forEach { offset ->
                drawCircle(color, ChartPointRadius.toPx(), offset)
                drawCircle(surfaceColor, ChartInnerPointRadius.toPx(), offset)
            }
            if (showValueLabels) {
                points.forEachIndexed { index, point ->
                    if (point.value > 0f) {
                        drawCenteredText(
                            textMeasurer = textMeasurer,
                            text = valueFormatter(point.value),
                            style = valueStyle,
                            centerX = offsets[index].x,
                            top = (offsets[index].y - 9.dp.toPx()).coerceAtLeast(top),
                        )
                    }
                }
            }
        }
        drawColumnLabels(
            labels = points.map { it.label },
            start = start,
            bottom = bottom,
            columnWidth = columnWidth,
            textMeasurer = textMeasurer,
            style = axisStyle,
        )
    }
}

@Composable
private fun BarChartPlot(
    pointLabels: List<String>,
    series: List<ChartSeries>,
    surfaceColor: Color,
    dividerColor: Color,
    labelColor: Color,
    axisMax: Float,
    showBarValueLabels: Boolean,
    valueFormatter: (Float) -> String,
) {
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = chartAxisTextStyle(labelColor)
    Canvas(Modifier.fillMaxSize()) {
        val start = ChartPlotPadding.toPx()
        val end = size.width - ChartPlotPadding.toPx()
        val top = ChartTopPadding.toPx()
        val bottom = size.height - ChartBottomPadding.toPx()
        val height = (bottom - top).coerceAtLeast(1f)
        val columnWidth = (end - start).coerceAtLeast(1f) / pointLabels.size.coerceAtLeast(1)
        drawChartGrid(start, end, top, bottom, dividerColor)
        if (axisMax > 0f && series.isNotEmpty()) {
            val barWidth = ChartBarWidth.toPx()
            val gap = ChartBarGap.toPx()
            val seriesCount = series.size
            val groupBarsWidth = barWidth * seriesCount + gap * (seriesCount - 1)
            series.forEachIndexed { seriesIndex, chartSeries ->
                val style = TextStyle(
                    color = chartSeries.color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                chartSeries.values.forEachIndexed { index, value ->
                    if (index >= pointLabels.size) return@forEachIndexed
                    if (value <= 0f) return@forEachIndexed
                    val barHeight = height * value / axisMax
                    val left = start + columnWidth * index +
                        (columnWidth - groupBarsWidth) / 2f +
                        seriesIndex * (barWidth + gap)
                    drawRoundRect(
                        color = chartSeries.color,
                        topLeft = Offset(left, bottom - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(ChartBarRadius.toPx()),
                    )
                    if (showBarValueLabels) {
                        drawCenteredText(
                            textMeasurer = textMeasurer,
                            text = valueFormatter(value),
                            style = style,
                            centerX = left + barWidth / 2f,
                            top = (bottom - barHeight - 3.dp.toPx()).coerceAtLeast(top),
                        )
                    }
                }
            }
        }
        drawColumnLabels(
            labels = pointLabels,
            start = start,
            bottom = bottom,
            columnWidth = columnWidth,
            textMeasurer = textMeasurer,
            style = axisStyle,
        )
    }
}

private fun DrawScope.drawChartGrid(
    start: Float,
    end: Float,
    top: Float,
    bottom: Float,
    color: Color,
) {
    val height = bottom - top
    repeat(CHART_AXIS_STEP_COUNT + 1) { index ->
        val y = top + height * index / CHART_AXIS_STEP_COUNT.toFloat()
        drawLine(color, Offset(start, y), Offset(end, y), 1.dp.toPx())
    }
}

private fun DrawScope.drawColumnLabels(
    labels: List<String>,
    start: Float,
    bottom: Float,
    columnWidth: Float,
    textMeasurer: TextMeasurer,
    style: TextStyle,
) {
    labels.forEachIndexed { index, label ->
        val layout = textMeasurer.measure(AnnotatedString(label), style)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = start + columnWidth * index + columnWidth / 2f - layout.size.width / 2f,
                y = bottom + 6.dp.toPx(),
            ),
        )
    }
}

private fun DrawScope.drawCenteredText(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    centerX: Float,
    top: Float,
) {
    val layout = textMeasurer.measure(AnnotatedString(text), style)
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(centerX - layout.size.width / 2f, top - layout.size.height),
    )
}

private fun DrawScope.drawTextAt(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    topLeft: Offset,
) {
    drawText(
        textLayoutResult = textMeasurer.measure(AnnotatedString(text), style),
        topLeft = topLeft,
    )
}

private fun chartAxisTextStyle(color: Color) = TextStyle(color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)

/** Rounds [maxValue] / [CHART_AXIS_STEP_COUNT] up to a "nice" step (1, 2, 3, 4, 5 or 10 times a
 *  power of ten) so the y-axis ticks land on readable values. */
private fun computeNiceAxisStep(maxValue: Float): Float {
    if (maxValue <= 0f) return 1f
    val target = maxValue.toDouble() / CHART_AXIS_STEP_COUNT
    val exponent = floor(log10(target)).toInt()
    val power = 10.0.pow(exponent.toDouble())
    val fraction = target / power
    val nice = when {
        fraction <= 1.0 -> 1.0
        fraction <= 2.0 -> 2.0
        fraction <= 3.0 -> 3.0
        fraction <= 4.0 -> 4.0
        fraction <= 5.0 -> 5.0
        else -> 10.0
    }
    return (nice * power).toFloat().coerceAtLeast(1f)
}
