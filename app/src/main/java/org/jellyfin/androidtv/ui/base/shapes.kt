package org.jellyfin.androidtv.ui.base

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

object ShapeDefaults {
	val ExtraSmall: CornerBasedShape = RoundedCornerShape(4.dp)
	val Small: CornerBasedShape = RoundedCornerShape(8.dp)
	val Medium: CornerBasedShape = RoundedCornerShape(12.dp)
	val Large: CornerBasedShape = RoundedCornerShape(16.dp)
	val ExtraLarge: CornerBasedShape = RoundedCornerShape(28.dp)
	val Dialog: CornerBasedShape = RoundedCornerShape(20.dp)
	val Button: CornerBasedShape = RoundedCornerShape(6.dp)
	val Full: CornerBasedShape = RoundedCornerShape(999.dp)
}

@Immutable
data class Shapes(
	val extraSmall: CornerBasedShape = ShapeDefaults.ExtraSmall,
	val small: CornerBasedShape = ShapeDefaults.Small,
	val medium: CornerBasedShape = ShapeDefaults.Medium,
	val large: CornerBasedShape = ShapeDefaults.Large,
	val extraLarge: CornerBasedShape = ShapeDefaults.ExtraLarge,
	val dialog: CornerBasedShape = ShapeDefaults.Dialog,
	val button: CornerBasedShape = ShapeDefaults.Button,
	val full: CornerBasedShape = ShapeDefaults.Full,
)

val LocalShapes = staticCompositionLocalOf { Shapes() }
