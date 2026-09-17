// Copyright 2026, yubeix contributors
// SPDX-License-Identifier: Apache-2.0

package site.unclefish.yubeix.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import site.unclefish.yubeix.icon.cupertino.CupertinoIcons
import site.unclefish.yubeix.icon.cupertino.outlined.ChevronBackward
import site.unclefish.yubeix.icon.cupertino.outlined.ChevronForward
import site.unclefish.yubeix.basic.CircularProgressIndicator
import site.unclefish.yubeix.basic.Icon
import site.unclefish.yubeix.basic.Text
import site.unclefish.yubeix.theme.YubeixTheme
import kotlin.math.abs

/** Default Monday-first, two-letter weekday captions. Override [MonthCalendar.weekdayLabels]
 *  with localized labels; commonMain cannot resolve the user's locale calendar names. */
private val DefaultWeekdayLabels = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

/** Day-of-week offsets for Sakamoto's algorithm, January..December. */
private val DaysBeforeMonth = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)

/**
 * A month grid calendar in iOS-grouped style: circular cells, a ring around the
 * [MonthCalendarState.highlightDayKey], a small dot under each [MonthCalendarState.markedDayKeys]
 * entry, and a direction-aware horizontal slide when the month changes.
 *
 * It only renders and reports taps; it owns no state about what the days mean. Callers
 * (check-in, streaks, logs, ...) decide semantics.
 *
 * Day keys follow the YYYYMMDD-as-Int encoding (`year * 10000 + month * 100 + day`); the calendar
 * itself has no notion of "today" or of any time zone — those belong to callers that know the
 * domain.
 */
data class MonthCalendarState(
    val year: Int,
    val month: Int,
    /** The "today" cell, drawn with a primary ring. */
    val highlightDayKey: Int,
    /** Days before this are inert (e.g. before the account existed). */
    val earliestDayKey: Int,
    /** Days drawn with a primary dot under the number. */
    val markedDayKeys: Set<Int>,
    /** Optional in-flight day, shows a spinner instead of the number. */
    val pendingDayKey: Int? = null,
)

/**
 * Renders a swipeable month calendar with previous/next navigation buttons, a fixed weekday
 * header, and a month grid whose height interpolates between the 4-row and 6-row layouts of the
 * neighbouring months while the pager slides.
 *
 * @param state The month to display plus its highlight, marks and pending day.
 * @param canGoPrevious Whether the caller allows moving to the previous month.
 * @param canGoNext Whether the caller allows moving to the next month.
 * @param onPreviousMonth Called when the previous-month button is tapped.
 * @param onNextMonth Called when the next-month button is tapped.
 * @param onMonthChange Called with the new `(year, month)` after a swipe settles on another month.
 * @param onDayClick Called with the tapped day's YYYYMMDD key.
 * @param monthTitle Formats the `(year, month)` caption between the navigation buttons. The
 *  default renders `"2026/9"`; supply a localized formatter for anything else.
 * @param weekdayLabels Seven Monday-first weekday captions for the fixed header.
 */
@Composable
fun MonthCalendar(
    state: MonthCalendarState,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthChange: (year: Int, month: Int) -> Unit,
    onDayClick: (dayKey: Int) -> Unit,
    modifier: Modifier = Modifier,
    monthTitle: (year: Int, month: Int) -> String = { year, month -> "$year/$month" },
    weekdayLabels: List<String> = DefaultWeekdayLabels,
) {
    val colors = YubeixTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CalendarNavButton(
                enabled = canGoPrevious && isAfterEarliestMonth(state),
                forward = false,
                onClick = onPreviousMonth,
            )
            Text(
                text = monthTitle(state.year, state.month),
                style = YubeixTheme.textStyles.body1.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            CalendarNavButton(
                enabled = canGoNext && isBeforeHighlightMonth(state),
                forward = true,
                onClick = onNextMonth,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fixed weekday header — stays put while the pager slides beneath it.
        WeekdayHeader(weekdayLabels)

        MonthTransition(
            state = state,
            onMonthChange = onMonthChange,
            onDayClick = onDayClick,
        )
    }
}

@Composable
private fun WeekdayHeader(labels: List<String>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.take(7).forEachIndexed { index, label ->
            val weekend = index >= 5
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = YubeixTheme.textStyles.footnote2,
                color = if (weekend) {
                    YubeixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.7f)
                } else {
                    YubeixTheme.colorScheme.onSurfaceVariantSummary
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CalendarNavButton(
    enabled: Boolean,
    forward: Boolean,
    onClick: () -> Unit,
) {
    val colors = YubeixTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (forward) {
                CupertinoIcons.Outlined.ChevronForward
            } else {
                CupertinoIcons.Outlined.ChevronBackward
            },
            contentDescription = null,
            tint = if (enabled) colors.onSurface else colors.onSurfaceVariantSummary.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun MonthTransition(
    state: MonthCalendarState,
    onMonthChange: (Int, Int) -> Unit,
    onDayClick: (Int) -> Unit,
) {
    // Use a finite pager: page 0 is the earliest allowed month and the last
    // page is the highlighted month. Future pages therefore do not exist, so no
    // targetPage clamp or snap-back loop is needed.
    val earliestYearMonth = remember(state.earliestDayKey) {
        yearMonthOf(state.earliestDayKey)
    }
    val highlightYearMonth = remember(state.highlightDayKey) {
        yearMonthOf(state.highlightDayKey)
    }
    val earliestYear = earliestYearMonth.first
    val earliestMonth = earliestYearMonth.second
    val highlightYear = highlightYearMonth.first
    val highlightMonth = highlightYearMonth.second

    val pageCount = remember(earliestYear, earliestMonth, highlightYear, highlightMonth) {
        (monthDistance(earliestYear, earliestMonth, highlightYear, highlightMonth) + 1)
            .coerceAtLeast(1)
    }
    val statePage = remember(state.year, state.month, earliestYear, earliestMonth, pageCount) {
        monthDistance(earliestYear, earliestMonth, state.year, state.month)
            .coerceIn(0, pageCount - 1)
    }

    key(earliestYear, earliestMonth, highlightYear, highlightMonth, pageCount) {
        val pagerState = rememberPagerState(initialPage = statePage) { pageCount }
        val pagerFlingBehavior = rememberPagerFlingBehavior(pagerState)

        val currentStatePage by rememberUpdatedState(statePage)
        val currentOnMonthChange by rememberUpdatedState(onMonthChange)

        // Only commit a month change after the pager has settled. A quick fling can
        // settle more than one page away from the acknowledged state, so commit the
        // actual settled page instead of reducing it to a single previous/next step.
        LaunchedEffect(pagerState, pageCount) {
            snapshotFlow { pagerState.settledPage }
                .collect { settledPage ->
                    val targetPage = settledPage.coerceIn(0, pageCount - 1)
                    if (targetPage != currentStatePage) {
                        val (year, month) = monthForPage(earliestYear, earliestMonth, targetPage)
                        currentOnMonthChange(year, month)
                    }
                }
        }

        // A navigation-button tap updates the external state first. Mirror that
        // acknowledged state into the pager. A swipe-generated state update lands
        // on the same page, so this does not fight the gesture.
        LaunchedEffect(statePage) {
            if (!pagerState.isScrollInProgress && pagerState.settledPage != statePage) {
                pagerState.animatePagerToPage(statePage)
            }
        }

        // The pager height tracks the swipe so a 4-row month and a 6-row month
        // interpolate smoothly instead of snapping.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val gridWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val cellPaddingPx = with(LocalDensity.current) { 2.dp.toPx() }
            val rowVPaddingPx = with(LocalDensity.current) { 3.dp.toPx() }
            val rowHeightPx = gridWidthPx / 7f + 2 * cellPaddingPx + 2 * rowVPaddingPx

            val currentPage = pagerState.currentPage.coerceIn(0, pageCount - 1)
            val currentMonth = monthForPage(earliestYear, earliestMonth, currentPage)
            val currentRows = weekRowCount(currentMonth.first, currentMonth.second)
            val direction = if (pagerState.currentPageOffsetFraction >= 0f) 1 else -1
            val neighbourPage = (currentPage + direction).coerceIn(0, pageCount - 1)
            val neighbourMonth = monthForPage(earliestYear, earliestMonth, neighbourPage)
            val neighbourRows = weekRowCount(neighbourMonth.first, neighbourMonth.second)
            val fraction = abs(pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
            val interpolatedRows = lerp(currentRows.toFloat(), neighbourRows.toFloat(), fraction)
            val heightDp = with(LocalDensity.current) { (interpolatedRows * rowHeightPx).toDp() }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heightDp),
                flingBehavior = pagerFlingBehavior,
                beyondViewportPageCount = 1,
            ) { page ->
                // Do not remember by page alone: state contains the loaded marks and
                // pending status, and must be allowed to refresh once the caller has
                // loaded the newly selected month.
                val monthState = stateForPage(state, earliestYear, earliestMonth, page)
                MonthGrid(state = monthState, onDayClick = onDayClick)
            }
        }
    }
}

/** Number of week rows a month occupies (Mon-first). */
private fun weekRowCount(year: Int, month: Int): Int {
    val lead = firstWeekdayIndex(year, month)
    val count = daysInMonth(year, month)
    return (lead + count + 6) / 7
}

private fun monthForPage(
    firstYear: Int,
    firstMonth: Int,
    page: Int,
): Pair<Int, Int> {
    val zeroBasedMonth = (firstMonth - 1) + page
    return (firstYear + zeroBasedMonth / 12) to (zeroBasedMonth % 12 + 1)
}

/** Whole months between (ay,am) and (by,bm); sign-aware. */
private fun monthDistance(ay: Int, am: Int, by: Int, bm: Int): Int {
    return (by - ay) * 12 + (bm - am)
}

private fun isAfterEarliestMonth(state: MonthCalendarState): Boolean {
    val earliest = yearMonthOf(state.earliestDayKey)
    return monthDistance(earliest.first, earliest.second, state.year, state.month) > 0
}

private fun isBeforeHighlightMonth(state: MonthCalendarState): Boolean {
    val highlight = yearMonthOf(state.highlightDayKey)
    return monthDistance(state.year, state.month, highlight.first, highlight.second) > 0
}

/** Build the state for a finite pager page. Only the externally acknowledged
 *  month carries loaded marks; neighbouring pages remain lightweight previews. */
private fun stateForPage(
    state: MonthCalendarState,
    firstYear: Int,
    firstMonth: Int,
    page: Int,
): MonthCalendarState {
    val (year, month) = monthForPage(firstYear, firstMonth, page)
    val isStateMonth = year == state.year && month == state.month
    return if (isStateMonth) {
        state.copy(year = year, month = month)
    } else {
        state.copy(
            year = year,
            month = month,
            markedDayKeys = emptySet(),
            pendingDayKey = null,
        )
    }
}

@Composable
private fun MonthGrid(
    state: MonthCalendarState,
    onDayClick: (Int) -> Unit,
) {
    val firstWeekday = remember(state.year, state.month) {
        firstWeekdayIndex(state.year, state.month)
    }
    val monthDayCount = remember(state.year, state.month) {
        daysInMonth(state.year, state.month)
    }
    val cells = remember(state.year, state.month) {
        List(firstWeekday) { null } + (1..monthDayCount).map { day ->
            dayKey(state.year, state.month, day)
        }
    }

    Column {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
            ) {
                (week + List(7 - week.size) { null }).forEach { dayKey ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayKey != null) {
                            MonthDayCell(
                                dayKey = dayKey,
                                state = state,
                                onDayClick = onDayClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    dayKey: Int,
    state: MonthCalendarState,
    onDayClick: (Int) -> Unit,
) {
    val colors = YubeixTheme.colorScheme
    val marked = dayKey in state.markedDayKeys
    val highlighted = dayKey == state.highlightDayKey
    val future = dayKey > state.highlightDayKey
    val beforeEarliest = dayKey < state.earliestDayKey
    val pending = state.pendingDayKey == dayKey
    val disabled = marked || future || beforeEarliest || state.pendingDayKey != null

    val textColor = when {
        marked || highlighted -> colors.primary
        future || beforeEarliest -> colors.onSurfaceVariantSummary.copy(alpha = 0.42f)
        else -> colors.onSurface
    }
    val fontWeight = if (marked || highlighted) FontWeight.SemiBold else FontWeight.Medium

    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .then(
                if (highlighted) {
                    Modifier
                        .clip(CircleShape)
                        .border(width = 1.dp, color = colors.primary, shape = CircleShape)
                } else {
                    Modifier
                },
            )
            .then(
                if (!disabled) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = { onDayClick(dayKey) },
                    )
                } else {
                    Modifier
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (pending) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = dayOfMonth(dayKey).toString(),
                style = YubeixTheme.textStyles.body2.copy(fontWeight = fontWeight),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        // Reserve space so the number stays centered whether or not the dot
        // is shown.
        Box(modifier = Modifier.size(5.dp)) {
            if (marked) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// YYYYMMDD day-key math (commonMain-safe replacements for the app's calendar
// utilities; Gregorian rules only, no time-zone or locale dependency).
// ---------------------------------------------------------------------------

private fun dayKey(year: Int, month: Int, day: Int): Int = year * 10_000 + month * 100 + day

private fun yearMonthOf(dayKey: Int): Pair<Int, Int> = dayKey / 10_000 to (dayKey / 100) % 100

private fun dayOfMonth(dayKey: Int): Int = dayKey % 100

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

/** Index of the 1st of [month] in a Monday-first week (Mon = 0 ... Sun = 6). */
private fun firstWeekdayIndex(year: Int, month: Int): Int {
    val adjustedYear = if (month < 3) year - 1 else year
    val sundayBased = (adjustedYear + adjustedYear / 4 - adjustedYear / 100 +
        adjustedYear / 400 + DaysBeforeMonth[month - 1] + 1) % 7
    return (sundayBased + 6) % 7
}
