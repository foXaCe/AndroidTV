package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

fun colorScheme(): ColorScheme =
	ColorScheme(
		// Background & Surface — VegafoX Dark Premium
		background = VegafoXColors.Background,
		onBackground = VegafoXColors.TextPrimary,
		surface = VegafoXColors.Surface,
		surfaceDim = VegafoXColors.SurfaceDim,
		surfaceBright = VegafoXColors.SurfaceBright,
		surfaceContainer = VegafoXColors.SurfaceContainer,
		onSurface = VegafoXColors.TextPrimary,
		onSurfaceVariant = VegafoXColors.TextSecondary,
		scrim = Color.Black.copy(alpha = 0.67f),
		// Primary — Orange VegafoX
		primary = VegafoXColors.OrangePrimary,
		primaryDark = VegafoXColors.OrangeDark,
		primaryLight = VegafoXColors.OrangeLight,
		primaryContainer = VegafoXColors.OrangeContainer,
		onPrimary = VegafoXColors.Background,
		onPrimaryContainer = VegafoXColors.OrangePrimary,
		// Secondary — Orange warm
		secondary = VegafoXColors.OrangeWarm,
		secondaryContainer = VegafoXColors.OrangeSoft,
		onSecondary = VegafoXColors.Background,
		onSecondaryContainer = VegafoXColors.OrangeLight,
		// Tertiary (ratings, stars)
		tertiary = VegafoXColors.Rating,
		onTertiary = Color(0xFF1A1400),
		// Error
		error = VegafoXColors.Error,
		errorContainer = VegafoXColors.ErrorContainer,
		onError = VegafoXColors.Background,
		onErrorContainer = Color(0xFFFFDAD9),
		// Success
		success = VegafoXColors.Success,
		successContainer = VegafoXColors.SuccessContainer,
		onSuccess = VegafoXColors.Background,
		// Warning
		warning = VegafoXColors.Warning,
		warningContainer = VegafoXColors.WarningContainer,
		onWarning = Color(0xFF1A1400),
		// Info
		info = VegafoXColors.Info,
		infoContainer = VegafoXColors.InfoContainer,
		onInfo = VegafoXColors.Background,
		// Text — warm off-white primary, muted secondary
		textPrimary = VegafoXColors.TextPrimary,
		textSecondary = VegafoXColors.TextSecondary,
		textDisabled = VegafoXColors.TextDisabled,
		textHint = VegafoXColors.TextHint,
		// Outline & Dividers — warm tint
		outline = VegafoXColors.Outline,
		outlineVariant = VegafoXColors.OutlineVariant,
		divider = VegafoXColors.Divider,
		// Dialog
		dialogScrim = VegafoXColors.DialogScrim,
		dialogSurface = VegafoXColors.DialogSurface,
		// Focus — Orange VegafoX
		focusRing = VegafoXColors.FocusRing,
		focusGlow = VegafoXColors.FocusGlow,
		// Toolbar
		toolbarBackground = VegafoXColors.ToolbarBackground,
		toolbarDivider = VegafoXColors.ToolbarDivider,
		// Rating
		rating = VegafoXColors.Rating,
		ratingEmpty = VegafoXColors.RatingEmpty,
		// Button — orange-tinted
		button = VegafoXColors.OrangeSoft,
		onButton = VegafoXColors.TextPrimary,
		buttonFocused = VegafoXColors.OrangePrimary,
		onButtonFocused = VegafoXColors.Background,
		buttonDisabled = Color(0x33747474),
		onButtonDisabled = VegafoXColors.TextDisabled,
		buttonActive = VegafoXColors.OrangeBorder,
		onButtonActive = VegafoXColors.TextPrimary,
		// Input
		input = Color(0xB3747474),
		onInput = VegafoXColors.TextPrimary,
		inputFocused = VegafoXColors.OrangeBorder,
		onInputFocused = VegafoXColors.TextPrimary,
		// Range Control & Seekbar
		rangeControlBackground = VegafoXColors.Outline,
		rangeControlFill = VegafoXColors.OrangePrimary,
		rangeControlKnob = VegafoXColors.TextPrimary,
		seekbarBuffer = VegafoXColors.TextSecondary,
		// Recording
		recording = VegafoXColors.Recording,
		onRecording = VegafoXColors.OnRecording,
		// Badge — Orange VegafoX
		badge = VegafoXColors.OrangePrimary,
		onBadge = VegafoXColors.TextPrimary,
		// List
		listHeader = VegafoXColors.TextPrimary,
		listOverline = VegafoXColors.TextSecondary,
		listHeadline = VegafoXColors.TextPrimary,
		listCaption = VegafoXColors.TextHint,
		listButton = Color.Transparent,
		listButtonFocused = Color(0x0FFFFFFF), // 6% white
		// Gradient (details pages) — warm orange tones
		gradientStart = VegafoXColors.GradientStart,
		gradientMid = VegafoXColors.GradientMid,
		gradientEnd = VegafoXColors.GradientEnd,
	)

@Immutable
data class ColorScheme(
	// Background & Surface
	val background: Color,
	val onBackground: Color,
	val surface: Color,
	val surfaceDim: Color,
	val surfaceBright: Color,
	val surfaceContainer: Color,
	val onSurface: Color,
	val onSurfaceVariant: Color,
	val scrim: Color,
	// Primary
	val primary: Color,
	val primaryDark: Color,
	val primaryLight: Color,
	val primaryContainer: Color,
	val onPrimary: Color,
	val onPrimaryContainer: Color,
	// Secondary
	val secondary: Color,
	val secondaryContainer: Color,
	val onSecondary: Color,
	val onSecondaryContainer: Color,
	// Tertiary
	val tertiary: Color,
	val onTertiary: Color,
	// Error
	val error: Color,
	val errorContainer: Color,
	val onError: Color,
	val onErrorContainer: Color,
	// Success
	val success: Color,
	val successContainer: Color,
	val onSuccess: Color,
	// Warning
	val warning: Color,
	val warningContainer: Color,
	val onWarning: Color,
	// Info
	val info: Color,
	val infoContainer: Color,
	val onInfo: Color,
	// Text
	val textPrimary: Color,
	val textSecondary: Color,
	val textDisabled: Color,
	val textHint: Color,
	// Outline & Dividers
	val outline: Color,
	val outlineVariant: Color,
	val divider: Color,
	// Dialog
	val dialogScrim: Color,
	val dialogSurface: Color,
	// Focus
	val focusRing: Color,
	val focusGlow: Color,
	// Toolbar
	val toolbarBackground: Color,
	val toolbarDivider: Color,
	// Rating
	val rating: Color,
	val ratingEmpty: Color,
	// Button
	val button: Color,
	val onButton: Color,
	val buttonFocused: Color,
	val onButtonFocused: Color,
	val buttonDisabled: Color,
	val onButtonDisabled: Color,
	val buttonActive: Color,
	val onButtonActive: Color,
	// Input
	val input: Color,
	val onInput: Color,
	val inputFocused: Color,
	val onInputFocused: Color,
	// Range Control & Seekbar
	val rangeControlBackground: Color,
	val rangeControlFill: Color,
	val rangeControlKnob: Color,
	val seekbarBuffer: Color,
	// Recording
	val recording: Color,
	val onRecording: Color,
	// Badge
	val badge: Color,
	val onBadge: Color,
	// List
	val listHeader: Color,
	val listOverline: Color,
	val listHeadline: Color,
	val listCaption: Color,
	val listButton: Color,
	val listButtonFocused: Color,
	// Gradient
	val gradientStart: Color,
	val gradientMid: Color,
	val gradientEnd: Color,
)

val LocalColorScheme = staticCompositionLocalOf { colorScheme() }
