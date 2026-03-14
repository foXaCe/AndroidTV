package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object TypographyDefaults {
	val Default: TextStyle = TextStyle.Default

	// Full type scale — TV-optimized sizes

	val Display: TextStyle =
		Default.copy(
			fontSize = 48.sp,
			lineHeight = 56.sp,
			fontWeight = FontWeight.W500,
		)

	val HeadlineLarge: TextStyle =
		Default.copy(
			fontSize = 32.sp,
			lineHeight = 40.sp,
			fontWeight = FontWeight.W500,
		)

	val HeadlineMedium: TextStyle =
		Default.copy(
			fontSize = 24.sp,
			lineHeight = 32.sp,
			fontWeight = FontWeight.W500,
		)

	val TitleLarge: TextStyle =
		Default.copy(
			fontSize = 20.sp,
			lineHeight = 28.sp,
			fontWeight = FontWeight.W600,
		)

	val TitleMedium: TextStyle =
		Default.copy(
			fontSize = 16.sp,
			lineHeight = 24.sp,
			fontWeight = FontWeight.W600,
		)

	val TitleSmall: TextStyle =
		Default.copy(
			fontSize = 14.sp,
			lineHeight = 20.sp,
			fontWeight = FontWeight.W600,
		)

	val BodyLarge: TextStyle =
		Default.copy(
			fontSize = 16.sp,
			lineHeight = 24.sp,
			fontWeight = FontWeight.W400,
		)

	val BodyMedium: TextStyle =
		Default.copy(
			fontSize = 14.sp,
			lineHeight = 20.sp,
			fontWeight = FontWeight.W400,
		)

	val BodySmall: TextStyle =
		Default.copy(
			fontSize = 12.sp,
			lineHeight = 16.sp,
			fontWeight = FontWeight.W400,
		)

	val LabelLarge: TextStyle =
		Default.copy(
			fontSize = 14.sp,
			lineHeight = 20.sp,
			fontWeight = FontWeight.W600,
		)

	val LabelMedium: TextStyle =
		Default.copy(
			fontSize = 12.sp,
			lineHeight = 16.sp,
			fontWeight = FontWeight.W500,
		)

	val LabelSmall: TextStyle =
		Default.copy(
			fontSize = 10.sp,
			lineHeight = 12.sp,
			fontWeight = FontWeight.W500,
			letterSpacing = 0.5.sp,
		)

	// Bold variants — for screen titles and hero text
	val DisplayBold: TextStyle = Display.copy(fontWeight = FontWeight.W700)
	val HeadlineLargeBold: TextStyle = HeadlineLarge.copy(fontWeight = FontWeight.W700)
	val HeadlineMediumBold: TextStyle = HeadlineMedium.copy(fontWeight = FontWeight.W700)

	// Legacy list styles (still used by base components)

	val ListHeader: TextStyle =
		Default.copy(
			fontSize = 15.sp,
			lineHeight = 20.sp,
			fontWeight = FontWeight.W700,
		)
	val ListOverline: TextStyle =
		Default.copy(
			fontSize = 10.sp,
			lineHeight = 12.sp,
			fontWeight = FontWeight.W600,
			letterSpacing = 0.65.sp,
		)
	val ListHeadline: TextStyle =
		Default.copy(
			fontSize = 14.sp,
			lineHeight = 20.sp,
			fontWeight = FontWeight.W600,
		)
	val ListCaption: TextStyle =
		Default.copy(
			fontSize = 11.sp,
			lineHeight = 14.sp,
			fontWeight = FontWeight.W500,
			letterSpacing = 0.1.sp,
		)

	val Badge: TextStyle =
		Default.copy(
			fontSize = 11.sp,
			fontWeight = FontWeight.W700,
			textAlign = TextAlign.Center,
		)
}

@Immutable
data class Typography(
	val default: TextStyle = TypographyDefaults.Default,
	// Full type scale
	val display: TextStyle = TypographyDefaults.Display,
	val headlineLarge: TextStyle = TypographyDefaults.HeadlineLarge,
	val headlineMedium: TextStyle = TypographyDefaults.HeadlineMedium,
	val titleLarge: TextStyle = TypographyDefaults.TitleLarge,
	val titleMedium: TextStyle = TypographyDefaults.TitleMedium,
	val titleSmall: TextStyle = TypographyDefaults.TitleSmall,
	val bodyLarge: TextStyle = TypographyDefaults.BodyLarge,
	val bodyMedium: TextStyle = TypographyDefaults.BodyMedium,
	val bodySmall: TextStyle = TypographyDefaults.BodySmall,
	val labelLarge: TextStyle = TypographyDefaults.LabelLarge,
	val labelMedium: TextStyle = TypographyDefaults.LabelMedium,
	val labelSmall: TextStyle = TypographyDefaults.LabelSmall,
	// Bold variants
	val displayBold: TextStyle = TypographyDefaults.DisplayBold,
	val headlineLargeBold: TextStyle = TypographyDefaults.HeadlineLargeBold,
	val headlineMediumBold: TextStyle = TypographyDefaults.HeadlineMediumBold,
	// Legacy
	val listHeader: TextStyle = TypographyDefaults.ListHeader,
	val listOverline: TextStyle = TypographyDefaults.ListOverline,
	val listHeadline: TextStyle = TypographyDefaults.ListHeadline,
	val listCaption: TextStyle = TypographyDefaults.ListCaption,
	val badge: TextStyle = TypographyDefaults.Badge,
)

val LocalTypography = staticCompositionLocalOf { Typography() }
