package af.namazia.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Single spacing/shape scale for the whole app. Screens use these instead of inventing
 * their own numbers, which is what keeps rhythm consistent across five tabs.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Minimum touch target — anything tappable must reach this. */
    val touchTarget = 48.dp
}

object Radii {
    val sm = RoundedCornerShape(10.dp)
    val md = RoundedCornerShape(14.dp)
    val lg = RoundedCornerShape(20.dp)
    val xl = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(50)
}
