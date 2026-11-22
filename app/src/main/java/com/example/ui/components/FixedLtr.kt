package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Layouts that place their children by ABSOLUTE left-to-right position.
 * They never read the layout direction, so a phone set to Persian/RTL (or any
 * system "force RTL" option) cannot mirror them.
 */

/** Children one after another from the left edge, vertically centred. */
@Composable
fun FixedLtrRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(loose) }
        val width = placeables.sumOf { it.width } + gap * (placeables.size - 1).coerceAtLeast(0)
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width.coerceIn(constraints.minWidth, constraints.maxWidth), height.coerceIn(constraints.minHeight, constraints.maxHeight)) {
            var x = 0
            placeables.forEach { p ->
                p.place(x, (height - p.height) / 2)
                x += p.width + gap
            }
        }
    }
}

/** Equal-width children filling the width, first child at the far left. */
@Composable
fun FixedLtrEqualRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val n = measurables.size.coerceAtLeast(1)
        val total = constraints.maxWidth
        val childW = ((total - gap * (n - 1)) / n).coerceAtLeast(0)
        val placeables = measurables.map {
            it.measure(Constraints(minWidth = childW, maxWidth = childW, minHeight = 0, maxHeight = constraints.maxHeight))
        }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(total, height) {
            placeables.forEachIndexed { i, p -> p.place(i * (childW + gap), 0) }
        }
    }
}

/** Left slot at the far left, right slot at the far right, centre slot just left of the right slot. */
@Composable
fun FixedLtrBar(
    modifier: Modifier = Modifier,
    gapToRight: Dp = 12.dp,
    left: @Composable () -> Unit,
    center: @Composable () -> Unit,
    right: @Composable () -> Unit
) {
    Layout(
        content = { left(); center(); right() },
        modifier = modifier
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val l = measurables[0].measure(loose)
        val r = measurables[2].measure(loose)
        val c = measurables[1].measure(loose)
        val width = constraints.maxWidth
        val height = maxOf(l.height, c.height, r.height)
        val gap = gapToRight.roundToPx()
        layout(width, height) {
            l.place(0, (height - l.height) / 2)
            r.place(width - r.width, (height - r.height) / 2)
            c.place(width - r.width - gap - c.width, (height - c.height) / 2)
        }
    }
}
