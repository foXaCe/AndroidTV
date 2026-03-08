package org.jellyfin.androidtv.ui.base

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import org.jellyfin.design.Tokens

fun colorScheme(): ColorScheme = ColorScheme(
	// Background & Surface — TV-optimized dark theme with blue-purple tint
	// Avoids pure black (LCD halo), softer on OLED, cohesive in dim lighting
	background = Color(0xFF0A0A0F),
	onBackground = Color(0xFFEEEEF5),
	surface = Color(0xFF12121A),
	surfaceDim = Color(0xFF0F0F18),
	surfaceBright = Color(0xFF1C1C28),
	surfaceContainer = Color(0xFF2A2A3A),
	onSurface = Color(0xFFEEEEF5),
	onSurfaceVariant = Color(0xFF8888AA),
	scrim = Color.Black.copy(alpha = 0.67f),

	// Primary
	primary = Tokens.Color.colorCyan500,
	primaryDark = Tokens.Color.colorCyan600,
	primaryLight = Tokens.Color.colorCyan400,
	primaryContainer = Tokens.Color.colorCyan900,
	onPrimary = Tokens.Color.colorWhite,
	onPrimaryContainer = Tokens.Color.colorCyan100,

	// Secondary
	secondary = Color(0xFFAA5CC3),
	secondaryContainer = Tokens.Color.colorPurple800,
	onSecondary = Tokens.Color.colorWhite,
	onSecondaryContainer = Tokens.Color.colorPurple100,

	// Tertiary (ratings, stars)
	tertiary = Color(0xFFFFD700),
	onTertiary = Color(0xFF1A1400),

	// Error
	error = Tokens.Color.colorRed400,
	errorContainer = Tokens.Color.colorRed850,
	onError = Tokens.Color.colorWhite,
	onErrorContainer = Tokens.Color.colorRed100,

	// Success
	success = Tokens.Color.colorGreen400,
	successContainer = Tokens.Color.colorGreen850,
	onSuccess = Tokens.Color.colorWhite,

	// Warning
	warning = Color(0xFFF0D400),
	warningContainer = Color(0xFF2E2600),
	onWarning = Color(0xFF1A1400),

	// Info
	info = Tokens.Color.colorBlue400,
	infoContainer = Tokens.Color.colorBlue800,
	onInfo = Tokens.Color.colorWhite,

	// Text — TV-optimized: off-white primary, purple-tinted secondary
	textPrimary = Color(0xFFEEEEF5),
	textSecondary = Color(0xFF8888AA),
	textDisabled = Color(0xFF5C5C78),
	textHint = Color(0xFF7E7EA6),

	// Outline & Dividers — blue-purple tint for cohesion
	outline = Color(0xFF3E3E55),
	outlineVariant = Color(0xFF2E2E42),
	divider = Color(0xFF222236),

	// Dialog
	dialogScrim = Color(0xE6141414),
	dialogSurface = Color(0xFF1C1C28),

	// Focus
	focusRing = Tokens.Color.colorCyan500,
	focusGlow = Tokens.Color.colorCyan500.copy(alpha = 0.2f),

	// Toolbar
	toolbarBackground = Color(0xCC0D1117),
	toolbarDivider = Tokens.Color.colorWhite.copy(alpha = 0.1f),

	// Rating
	rating = Color(0xFFFFD700),
	ratingEmpty = Color(0xFF3E3E55),

	// Button
	button = Color(0xB3747474),
	onButton = Color(0xFFDDDDDD),
	buttonFocused = Color(0xE6CCCCCC),
	onButtonFocused = Color(0xFF444444),
	buttonDisabled = Color(0x33747474),
	onButtonDisabled = Color(0xFF686868),
	buttonActive = Color(0x4DCCCCCC),
	onButtonActive = Color(0xFFDDDDDD),

	// Input
	input = Color(0xB3747474),
	onInput = Color(0xE6CCCCCC),
	inputFocused = Color(0xE6CCCCCC),
	onInputFocused = Color(0xFFDDDDDD),

	// Range Control & Seekbar
	rangeControlBackground = Color(0xFF3E3E55),
	rangeControlFill = Tokens.Color.colorCyan500,
	rangeControlKnob = Color(0xFFEEEEF5),
	seekbarBuffer = Tokens.Color.colorBluegrey300,

	// Recording
	recording = Tokens.Color.colorRed300,
	onRecording = Tokens.Color.colorRed25,

	// Badge
	badge = Tokens.Color.colorCyan500,
	onBadge = Color(0xFFEEEEF5),

	// List
	listHeader = Color(0xFFEEEEF5),
	listOverline = Color(0xFF8888AA),
	listHeadline = Color(0xFFEEEEF5),
	listCaption = Color(0xFFB0B0CC),
	listButton = Color.Transparent,
	listButtonFocused = Color(0xFF2A2A3A),

	// Gradient (details pages)
	gradientStart = Color(0xFF0F3460),
	gradientMid = Color(0xFF0B2545),
	gradientEnd = Color(0xFF0A0A0F),
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
