package org.jellyfin.androidtv.ui.search.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.LocalTextStyle
import org.jellyfin.androidtv.ui.base.ProvideTextStyle
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun SearchTextInput(
	query: String,
	onQueryChange: (query: String) -> Unit,
	onQuerySubmit: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val focused by interactionSource.collectIsFocusedAsState()
	val keyboardController = LocalSoftwareKeyboardController.current
	var isEditing by remember { mutableStateOf(false) }
	var isFirstFocus by remember { mutableStateOf(true) }

	// Auto-open keyboard on first focus
	LaunchedEffect(focused) {
		if (focused && isFirstFocus) {
			isFirstFocus = false
			isEditing = true
		}
	}

	val borderColor = if (focused) VegafoXColors.OrangePrimary else Color.White.copy(alpha = 0.10f)
	val iconTint = if (focused) VegafoXColors.OrangePrimary else VegafoXColors.TextSecondary

	ProvideTextStyle(
		LocalTextStyle.current.copy(
			color = VegafoXColors.TextPrimary,
			fontSize = 16.sp,
		),
	) {
		BasicTextField(
			modifier =
				modifier
					.onKeyEvent { event ->
						if (focused &&
							!isEditing &&
							event.type == KeyEventType.KeyDown &&
							(event.key == Key.Enter || event.key == Key.DirectionCenter)
						) {
							isEditing = true
							true
						} else {
							false
						}
					}.onFocusChanged {
						if (!it.isFocused) {
							isEditing = false
							keyboardController?.hide()
						}
					},
			value = query,
			singleLine = true,
			readOnly = !isEditing,
			interactionSource = interactionSource,
			onValueChange = { onQueryChange(it) },
			keyboardActions =
				KeyboardActions {
					isEditing = false
					keyboardController?.hide()
					onQuerySubmit()
				},
			keyboardOptions =
				KeyboardOptions.Default.copy(
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Search,
					autoCorrectEnabled = true,
				),
			textStyle = LocalTextStyle.current,
			cursorBrush = SolidColor(VegafoXColors.OrangePrimary),
			decorationBox = { innerTextField ->
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier =
						Modifier
							.background(VegafoXColors.Surface, RoundedCornerShape(12.dp))
							.border(1.dp, borderColor, RoundedCornerShape(12.dp))
							.padding(12.dp),
				) {
					Icon(VegafoXIcons.Search, contentDescription = null, tint = iconTint)
					Spacer(Modifier.width(12.dp))
					innerTextField()
				}
			},
		)
	}
}
