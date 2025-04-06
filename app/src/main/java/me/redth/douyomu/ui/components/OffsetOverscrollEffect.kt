package me.redth.douyomu.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun rememberOffsetOverscroll(): OffsetOverscrollEffect {
    val scope = rememberCoroutineScope()
    return remember { OffsetOverscrollEffect(scope) }
}

@OptIn(ExperimentalFoundationApi::class)
class OffsetOverscrollEffect(val scope: CoroutineScope) : OverscrollEffect {
    private val overscrollOffset = Animatable(0f)

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        // in pre scroll we relax the overscroll if needed
        // relaxation: when we are in progress of the overscroll and user scrolls in the
        // different direction = substract the overscroll first
        val sameDirection = sign(delta.x) == sign(overscrollOffset.value)
        val consumedByPreScroll =
            if (abs(overscrollOffset.value) > 0.5 && !sameDirection) {
                val prevOverscrollValue = overscrollOffset.value
                val newOverscrollValue = overscrollOffset.value + delta.x
                if (sign(prevOverscrollValue) != sign(newOverscrollValue)) {
                    // sign changed, coerce to start scrolling and exit
                    scope.launch { overscrollOffset.snapTo(0f) }
                    Offset(x = delta.x + prevOverscrollValue, y = 0f)
                } else {
                    scope.launch { overscrollOffset.snapTo(overscrollOffset.value + delta.x) }
                    delta.copy(y = 0f)
                }
            } else {
                Offset.Zero
            }
        val leftForScroll = delta - consumedByPreScroll
        val consumedByScroll = performScroll(leftForScroll)
        val overscrollDelta = leftForScroll - consumedByScroll
        // if it is a drag, not a fling, add the delta left to our over scroll value
        if (abs(overscrollDelta.x) > 0.5 && source == NestedScrollSource.UserInput) {
            scope.launch {
                // multiply by 0.1 for the sake of parallax effect
                overscrollOffset.snapTo(overscrollOffset.value + overscrollDelta.x * 0.1f)
            }
        }
        return consumedByPreScroll + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        val consumed = performFling(velocity)
        // when the fling happens - we just gradually animate our overscroll to 0
        val remaining = velocity - consumed
        overscrollOffset.animateTo(
            targetValue = 0f,
            initialVelocity = remaining.x,
            animationSpec = spring()
        )
    }

    override val isInProgress: Boolean
        get() = overscrollOffset.value != 0f

    override val effectModifier: Modifier
        get() = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            return@layout layout(placeable.width, placeable.height) {
                val offsetValue = IntOffset(x = overscrollOffset.value.roundToInt(), y = 0)
                placeable.placeRelativeWithLayer(offsetValue.x, offsetValue.y)
            }
        }
}
