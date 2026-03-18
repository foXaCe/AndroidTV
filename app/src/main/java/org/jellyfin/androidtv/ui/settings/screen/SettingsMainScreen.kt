package org.jellyfin.androidtv.ui.settings.screen

import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.preference.PreferenceManager
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.constant.getQualityProfiles
import org.jellyfin.androidtv.data.repository.ExternalAppRepository
import org.jellyfin.androidtv.data.service.UpdateCheckerService
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.LocalShapes
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.Checkbox
import org.jellyfin.androidtv.ui.base.form.RangeControl
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListControl
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.SettingsDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.compat.rememberPreference
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.jellyfin.androidtv.ui.settings.screen.playback.getResumeSubtractDurationOptions
import org.jellyfin.androidtv.ui.settings.screen.screensaver.getScreensaverTimeoutOptions
import org.jellyfin.androidtv.ui.settings.screen.vegafox.getBlurLabel
import org.jellyfin.androidtv.ui.settings.screen.vegafox.getShuffleContentTypeLabel
import org.jellyfin.androidtv.util.isTvDevice
import org.jellyfin.design.Tokens
import org.koin.compose.koinInject
import java.text.DecimalFormat
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun SettingsMainScreen(
	userPreferences: UserPreferences = koinInject(),
	updateChecker: UpdateCheckerService = koinInject(),
	userRepository: UserRepository = koinInject(),
	externalAppRepository: ExternalAppRepository = koinInject(),
) {
	val router = LocalRouter.current
	val context = LocalContext.current
	val userId =
		userRepository.currentUser
			.collectAsState()
			.value
			?.id
	val userSettingPreferences = remember(userId) { UserSettingPreferences(context, userId) }

	var updateInfoForDialog by remember { mutableStateOf<UpdateCheckerService.UpdateInfo?>(null) }
	var showReleaseNotes by remember { mutableStateOf(false) }
	var showResetDialog by remember { mutableStateOf(false) }
	val isTvDevice = remember(context) { context.isTvDevice() }

	SettingsColumn {
		// ── Header (logo + title) ──
		item {
			Row(
				modifier =
					Modifier
						.padding(
							horizontal = SettingsDimensions.headerPaddingHorizontal,
							vertical = SettingsDimensions.headerPaddingVertical,
						).height(SettingsDimensions.headerHeight),
				horizontalArrangement = Arrangement.spacedBy(SettingsDimensions.headerGap),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Image(
					painter = painterResource(R.drawable.ic_vegafox_fox),
					contentDescription = null,
					modifier =
						Modifier
							.size(SettingsDimensions.headerLogoSize)
							.clip(CircleShape),
				)
				Column {
					Text(
						text = "VegafoX",
						style =
							JellyfinTheme.typography.titleLarge.copy(
								fontFamily = BebasNeue,
								color = VegafoXColors.OrangePrimary,
								letterSpacing = SettingsDimensions.sectionLetterSpacing,
							),
					)
					Text(
						text = stringResource(R.string.settings),
						style =
							JellyfinTheme.typography.bodySmall.copy(
								color = VegafoXColors.TextSecondary,
							),
					)
				}
			}
		}

		// ═══════════════════════════════════════════
		// ── 1. APPARENCE ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_appearance)) }) }

		item {
			var focusColor by rememberPreference(userSettingPreferences, UserSettingPreferences.focusColor)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_focus_color)) },
				captionContent = { Text(stringResource(focusColor.nameRes)) },
				onClick = { router.push(Routes.CUSTOMIZATION_THEME) },
			)
		}

		item {
			var backdropEnabled by rememberPreference(userPreferences, UserPreferences.backdropEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_show_backdrop)) },
				captionContent = { Text(stringResource(R.string.pref_show_backdrop_description)) },
				trailingContent = { Checkbox(checked = backdropEnabled) },
				onClick = { backdropEnabled = !backdropEnabled },
			)
		}

		item {
			val detailsBlur by rememberPreference(userSettingPreferences, UserSettingPreferences.detailsBackgroundBlurAmount)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_details_background_blur_amount)) },
				captionContent = { Text(getBlurLabel(detailsBlur)) },
				onClick = { router.push(Routes.VEGAFOX_DETAILS_BLUR) },
			)
		}

		item {
			val browsingBlur by rememberPreference(userSettingPreferences, UserSettingPreferences.browsingBackgroundBlurAmount)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_browsing_background_blur_amount)) },
				captionContent = { Text(getBlurLabel(browsingBlur)) },
				onClick = { router.push(Routes.VEGAFOX_BROWSING_BLUR) },
			)
		}

		item {
			var watchedIndicatorBehavior by rememberPreference(userPreferences, UserPreferences.watchedIndicatorBehavior)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_watched_indicator)) },
				captionContent = { Text(stringResource(watchedIndicatorBehavior.nameRes)) },
				onClick = { router.push(Routes.CUSTOMIZATION_WATCHED_INDICATOR) },
			)
		}

		item {
			var clockBehavior by rememberPreference(userPreferences, UserPreferences.clockBehavior)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_clock_display)) },
				captionContent = { Text(stringResource(clockBehavior.nameRes)) },
				onClick = { router.push(Routes.CUSTOMIZATION_CLOCK) },
			)
		}

		// ═══════════════════════════════════════════
		// ── 2. ACCUEIL ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.home_prefs)) }) }

		item {
			val posterSize by rememberPreference(userPreferences, UserPreferences.posterSize)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_poster_size)) },
				captionContent = { Text(stringResource(posterSize.nameRes)) },
				onClick = { router.push(Routes.HOME_POSTER_SIZE) },
			)
		}

		item {
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_home_rows_image_type)) },
				onClick = { router.push(Routes.HOME_ROWS_IMAGE_TYPE) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Home), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.home_section_settings)) },
				onClick = { router.push(Routes.HOME) },
			)
		}

		// ═══════════════════════════════════════════
		// ── 3. NAVIGATION ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_section_navigation)) }) }

		item {
			var showShuffleButton by rememberPreference(userPreferences, UserPreferences.showShuffleButton)
			val shuffleContentType by rememberPreference(userPreferences, UserPreferences.shuffleContentType)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_shuffle_button)) },
				captionContent = {
					Text(
						if (showShuffleButton) {
							getShuffleContentTypeLabel(shuffleContentType)
						} else {
							stringResource(R.string.lbl_off)
						},
					)
				},
				trailingContent = { Checkbox(checked = showShuffleButton) },
				onClick = {
					if (!showShuffleButton) {
						showShuffleButton = true
					} else {
						router.push(Routes.VEGAFOX_SHUFFLE_CONTENT_TYPE)
					}
				},
			)
		}

		item {
			var showGenresButton by rememberPreference(userPreferences, UserPreferences.showGenresButton)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_genres_button)) },
				trailingContent = { Checkbox(checked = showGenresButton) },
				onClick = { showGenresButton = !showGenresButton },
			)
		}

		item {
			var showFavoritesButton by rememberPreference(userPreferences, UserPreferences.showFavoritesButton)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_favorites_button)) },
				trailingContent = { Checkbox(checked = showFavoritesButton) },
				onClick = { showFavoritesButton = !showFavoritesButton },
			)
		}

		item {
			var showLibrariesInToolbar by rememberPreference(userPreferences, UserPreferences.showLibrariesInToolbar)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_libraries_in_toolbar)) },
				trailingContent = { Checkbox(checked = showLibrariesInToolbar) },
				onClick = { showLibrariesInToolbar = !showLibrariesInToolbar },
			)
		}

		item {
			var enableFolderView by rememberPreference(userPreferences, UserPreferences.enableFolderView)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_enable_folder_view)) },
				captionContent = { Text(stringResource(R.string.pref_enable_folder_view_description)) },
				trailingContent = { Checkbox(checked = enableFolderView) },
				onClick = { enableFolderView = !enableFolderView },
			)
		}

		item {
			var confirmExit by rememberPreference(userPreferences, UserPreferences.confirmExit)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_confirm_exit)) },
				trailingContent = { Checkbox(checked = confirmExit) },
				onClick = { confirmExit = !confirmExit },
			)
		}

		// ═══════════════════════════════════════════
		// ── 4. LECTURE ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_playback)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.TvPlay), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.playback_video_player)) },
				trailingContent = {
					val iconDrawable =
						remember(context) {
							externalAppRepository.getCurrentExternalPlayerApp(context)?.loadIcon(context.packageManager)
						}
					Image(
						painter =
							if (iconDrawable == null) {
								rememberAsyncImagePainter(R.mipmap.app_icon)
							} else {
								rememberAsyncImagePainter(iconDrawable)
							},
						contentDescription = null,
						modifier =
							Modifier
								.size(SettingsDimensions.playerIconSize)
								.clip(LocalShapes.current.small),
					)
				},
				onClick = { router.push(Routes.PLAYBACK_PLAYER) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.NextUp), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback_next_up)) },
				onClick = { router.push(Routes.PLAYBACK_NEXT_UP) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Subtitles), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_customization_subtitles)) },
				onClick = { router.push(Routes.CUSTOMIZATION_SUBTITLES) },
			)
		}

		item {
			var subtitlesDefaultToNone by rememberPreference(userPreferences, UserPreferences.subtitlesDefaultToNone)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_subtitles_default_to_none)) },
				captionContent = { Text(stringResource(R.string.pref_subtitles_default_to_none_description)) },
				trailingContent = { Checkbox(checked = subtitlesDefaultToNone) },
				onClick = { subtitlesDefaultToNone = !subtitlesDefaultToNone },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Clapperboard), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback_media_segments)) },
				onClick = { router.push(Routes.PLAYBACK_MEDIA_SEGMENTS) },
			)
		}

		item {
			var cinemaModeEnabled by rememberPreference(userPreferences, UserPreferences.cinemaModeEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_prerolls_enabled)) },
				trailingContent = { Checkbox(checked = cinemaModeEnabled) },
				onClick = { cinemaModeEnabled = !cinemaModeEnabled },
			)
		}

		item {
			var stillWatchingBehavior by rememberPreference(userPreferences, UserPreferences.stillWatchingBehavior)
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Sleep), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback_inactivity_prompt)) },
				captionContent = { Text(stringResource(stillWatchingBehavior.nameRes)) },
				onClick = { router.push(Routes.PLAYBACK_INACTIVITY_PROMPT) },
			)
		}

		item {
			var showDescriptionOnPause by rememberPreference(userSettingPreferences, UserSettingPreferences.showDescriptionOnPause)
			ListButton(
				headingContent = { Text(stringResource(R.string.show_description_on_pause)) },
				trailingContent = { Checkbox(checked = showDescriptionOnPause) },
				onClick = { showDescriptionOnPause = !showDescriptionOnPause },
			)
		}

		item {
			var skipForwardLength by rememberPreference(userSettingPreferences, UserSettingPreferences.skipForwardLength)
			val interactionSource = remember { MutableInteractionSource() }
			ListControl(
				headingContent = { Text(stringResource(R.string.skip_forward_length)) },
				interactionSource = interactionSource,
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					RangeControl(
						modifier = Modifier.height(SettingsDimensions.sliderHeight).weight(1f),
						interactionSource = interactionSource,
						min = 5_000f,
						max = 30_000f,
						stepForward = 5_000f,
						value = skipForwardLength.toFloat(),
						onValueChange = { skipForwardLength = it.roundToInt() },
					)
					Spacer(Modifier.width(Tokens.Space.spaceSm))
					Box(
						modifier = Modifier.sizeIn(minWidth = SettingsDimensions.sliderMinValueWidth),
						contentAlignment = Alignment.CenterEnd,
					) { Text("${skipForwardLength / 1000}s") }
				}
			}
		}

		item {
			var unpauseRewindDuration by rememberPreference(userSettingPreferences, UserSettingPreferences.unpauseRewindDuration)
			val interactionSource = remember { MutableInteractionSource() }
			ListControl(
				headingContent = { Text(stringResource(R.string.unpause_rewind_duration)) },
				interactionSource = interactionSource,
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					RangeControl(
						modifier = Modifier.height(SettingsDimensions.sliderHeight).weight(1f),
						interactionSource = interactionSource,
						min = 0f,
						max = 10_000f,
						stepForward = 1_000f,
						value = unpauseRewindDuration.toFloat(),
						onValueChange = { unpauseRewindDuration = it.roundToInt() },
					)
					Spacer(Modifier.width(Tokens.Space.spaceSm))
					Box(
						modifier = Modifier.sizeIn(minWidth = SettingsDimensions.sliderMinValueWidth),
						contentAlignment = Alignment.CenterEnd,
					) { Text("${unpauseRewindDuration / 1000}s") }
				}
			}
		}

		item {
			var resumeSubtractDuration by rememberPreference(userPreferences, UserPreferences.resumeSubtractDuration)
			val options = getResumeSubtractDurationOptions()
			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_resume_preroll)) },
				captionContent = { Text(options[resumeSubtractDuration].orEmpty()) },
				onClick = { router.push(Routes.PLAYBACK_RESUME_SUBTRACT_DURATION) },
			)
		}

		// ═══════════════════════════════════════════
		// ── 5. VIDÉO & AUDIO ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_section_video_audio)) }) }

		item {
			var maxBitrate by rememberPreference(userPreferences, UserPreferences.maxBitrate)
			val options = getQualityProfiles(context)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_max_bitrate_title)) },
				captionContent = { Text(options[maxBitrate].orEmpty()) },
				onClick = { router.push(Routes.PLAYBACK_MAX_BITRATE) },
			)
		}

		item {
			var maxVideoResolution by rememberPreference(userPreferences, UserPreferences.maxVideoResolution)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_max_resolution_title)) },
				captionContent = { Text(stringResource(maxVideoResolution.nameRes)) },
				onClick = { router.push(Routes.PLAYBACK_MAX_RESOLUTION) },
			)
		}

		item {
			var playerZoomMode by rememberPreference(userPreferences, UserPreferences.playerZoomMode)
			ListButton(
				headingContent = { Text(stringResource(R.string.default_video_zoom)) },
				captionContent = { Text(stringResource(playerZoomMode.nameRes)) },
				onClick = { router.push(Routes.PLAYBACK_ZOOM_MODE) },
			)
		}

		item {
			var refreshRateSwitchingBehavior by rememberPreference(userPreferences, UserPreferences.refreshRateSwitchingBehavior)
			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_refresh_switching)) },
				captionContent = { Text(stringResource(refreshRateSwitchingBehavior.nameRes)) },
				onClick = { router.push(Routes.PLAYBACK_REFRESH_RATE_SWITCHING_BEHAVIOR) },
			)
		}

		item {
			var videoStartDelay by rememberPreference(userPreferences, UserPreferences.videoStartDelay)
			val interactionSource = remember { MutableInteractionSource() }
			ListControl(
				headingContent = { Text(stringResource(R.string.video_start_delay)) },
				interactionSource = interactionSource,
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					RangeControl(
						modifier = Modifier.height(SettingsDimensions.sliderHeight).weight(1f),
						interactionSource = interactionSource,
						min = 0_000f,
						max = 5_000f,
						stepForward = 250f,
						value = videoStartDelay.toFloat(),
						onValueChange = { videoStartDelay = it.roundToLong() },
					)
					Spacer(Modifier.width(Tokens.Space.spaceSm))
					Box(
						modifier = Modifier.sizeIn(minWidth = SettingsDimensions.sliderMinValueWidthWide),
						contentAlignment = Alignment.CenterEnd,
					) {
						val formatter = remember { DecimalFormat("0.##") }
						Text("${formatter.format(videoStartDelay / 1000f)}s")
					}
				}
			}
		}

		item {
			var audioBehaviour by rememberPreference(userPreferences, UserPreferences.audioBehaviour)
			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_audio_output)) },
				captionContent = { Text(stringResource(audioBehaviour.nameRes)) },
				onClick = { router.push(Routes.PLAYBACK_AUDIO_BEHAVIOR) },
			)
		}

		item {
			var audioNightMode by rememberPreference(userPreferences, UserPreferences.audioNightMode)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_audio_night_mode)) },
				trailingContent = { Checkbox(checked = audioNightMode) },
				onClick = { audioNightMode = !audioNightMode },
			)
		}

		item {
			var ac3Enabled by rememberPreference(userPreferences, UserPreferences.ac3Enabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_bitstream_ac3)) },
				trailingContent = { Checkbox(checked = ac3Enabled) },
				onClick = { ac3Enabled = !ac3Enabled },
			)
		}

		item {
			var liveTvDirectPlayEnabled by rememberPreference(userPreferences, UserPreferences.liveTvDirectPlayEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_direct_stream_live)) },
				trailingContent = { Checkbox(checked = liveTvDirectPlayEnabled) },
				onClick = { liveTvDirectPlayEnabled = !liveTvDirectPlayEnabled },
			)
		}

		item {
			var pgsDirectPlay by rememberPreference(userPreferences, UserPreferences.pgsDirectPlay)
			ListButton(
				headingContent = { Text(stringResource(R.string.preference_enable_pgs)) },
				trailingContent = { Checkbox(checked = pgsDirectPlay) },
				onClick = { pgsDirectPlay = !pgsDirectPlay },
			)
		}

		item {
			var assDirectPlay by rememberPreference(userPreferences, UserPreferences.assDirectPlay)
			ListButton(
				headingContent = { Text(stringResource(R.string.preference_enable_libass)) },
				trailingContent = { Checkbox(checked = assDirectPlay) },
				onClick = { assDirectPlay = !assDirectPlay },
			)
		}

		// ═══════════════════════════════════════════
		// ── 6. ÉCRAN DE VEILLE ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_screensaver)) }) }

		item {
			var screensaverInAppEnabled by rememberPreference(userPreferences, UserPreferences.screensaverInAppEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_screensaver_inapp_enabled)) },
				captionContent = { Text(stringResource(R.string.pref_screensaver_inapp_enabled_description)) },
				trailingContent = { Checkbox(checked = screensaverInAppEnabled) },
				onClick = { screensaverInAppEnabled = !screensaverInAppEnabled },
			)
		}

		item {
			var screensaverInAppTimeout by rememberPreference(userPreferences, UserPreferences.screensaverInAppTimeout)
			val caption =
				getScreensaverTimeoutOptions()
					.firstOrNull { (duration) -> duration.inWholeMilliseconds == screensaverInAppTimeout }
					?.second
					.orEmpty()
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_screensaver_inapp_timeout)) },
				captionContent = { Text(caption) },
				onClick = { router.push(Routes.CUSTOMIZATION_SCREENSAVER_TIMEOUT) },
			)
		}

		item {
			var screensaverMode by rememberPreference(userPreferences, UserPreferences.screensaverMode)
			val caption =
				when (screensaverMode) {
					"library" -> stringResource(R.string.pref_screensaver_mode_library)
					"logo" -> stringResource(R.string.pref_screensaver_mode_logo)
					else -> screensaverMode
				}
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_screensaver_mode)) },
				captionContent = { Text(caption) },
				onClick = { router.push(Routes.CUSTOMIZATION_SCREENSAVER_MODE) },
			)
		}

		item {
			var screensaverDimmingLevel by rememberPreference(userPreferences, UserPreferences.screensaverDimmingLevel)
			val caption = if (screensaverDimmingLevel == 0) stringResource(R.string.lbl_off) else "$screensaverDimmingLevel%"
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_screensaver_dimming)) },
				captionContent = { Text(caption) },
				onClick = { router.push(Routes.CUSTOMIZATION_SCREENSAVER_DIMMING) },
			)
		}

		item {
			var screensaverAgeRatingRequired by rememberPreference(userPreferences, UserPreferences.screensaverAgeRatingRequired)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_screensaver_ageratingrequired_title)) },
				captionContent = { Text(stringResource(R.string.pref_screensaver_ageratingrequired_enabled)) },
				trailingContent = { Checkbox(checked = screensaverAgeRatingRequired) },
				onClick = { screensaverAgeRatingRequired = !screensaverAgeRatingRequired },
			)
		}

		item {
			var screensaverShowClock by rememberPreference(userPreferences, UserPreferences.screensaverShowClock)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_screensaver_show_clock)) },
				trailingContent = { Checkbox(checked = screensaverShowClock) },
				onClick = { screensaverShowClock = !screensaverShowClock },
			)
		}

		// ═══════════════════════════════════════════
		// ── 7. JELLYSEERR ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.jellyseerr)) }) }

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_jellyseerr_jellyfish), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.jellyseerr_settings)) },
				captionContent = { Text(stringResource(R.string.jellyseerr_settings_description)) },
				onClick = { router.push(Routes.JELLYSEERR) },
			)
		}

		// ═══════════════════════════════════════════
		// ── 8. SYNCPLAY ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.syncplay)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.SyncPlay), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.syncplay)) },
				captionContent = { Text(stringResource(R.string.syncplay_description)) },
				onClick = { router.push(Routes.VEGAFOX_SYNCPLAY) },
			)
		}

		// ═══════════════════════════════════════════
		// ── 9. COMPTES ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_login)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Group), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_login)) },
				onClick = { router.push(Routes.AUTHENTICATION) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Lock), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_parental_controls)) },
				captionContent = { Text(stringResource(R.string.pref_parental_controls_description)) },
				onClick = { router.push(Routes.VEGAFOX_PARENTAL_CONTROLS) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.GridView), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_libraries)) },
				onClick = { router.push(Routes.LIBRARIES) },
			)
		}

		// ═══════════════════════════════════════════
		// ── 10. À PROPOS & AVANCÉ ──
		// ═══════════════════════════════════════════
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_section_about_advanced)) }) }

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_jellyfin), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_about_title)) },
				onClick = { router.push(Routes.ABOUT) },
			)
		}

		if (org.jellyfin.androidtv.BuildConfig.ENABLE_OTA_UPDATES) {
			item {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Download), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.settings_check_updates)) },
					captionContent = { Text(stringResource(R.string.settings_check_updates_desc)) },
					onClick = {
						checkForUpdates(context, updateChecker) { info ->
							updateInfoForDialog = info
						}
					},
				)
			}

			item {
				var updateNotificationsEnabled by rememberPreference(userPreferences, UserPreferences.updateNotificationsEnabled)
				ListButton(
					headingContent = { Text(stringResource(R.string.settings_update_notifications)) },
					captionContent = { Text(stringResource(R.string.settings_update_notifications_desc)) },
					trailingContent = { Checkbox(checked = updateNotificationsEnabled) },
					onClick = { updateNotificationsEnabled = !updateNotificationsEnabled },
				)
			}
		}

		item {
			var trickPlayEnabled by rememberPreference(userPreferences, UserPreferences.trickPlayEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.preference_enable_trickplay)) },
				captionContent = { Text(stringResource(R.string.enable_playback_module_description)) },
				trailingContent = { Checkbox(checked = trickPlayEnabled) },
				onClick = { trickPlayEnabled = !trickPlayEnabled },
			)
		}

		item {
			var debuggingEnabled by rememberPreference(userPreferences, UserPreferences.debuggingEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_enable_debug)) },
				captionContent = { Text(stringResource(R.string.desc_debug)) },
				trailingContent = { Checkbox(checked = debuggingEnabled) },
				onClick = { debuggingEnabled = !debuggingEnabled },
			)
		}

		item {
			var preferExoPlayerFfmpeg by rememberPreference(userPreferences, UserPreferences.preferExoPlayerFfmpeg)
			ListButton(
				headingContent = { Text(stringResource(R.string.prefer_exoplayer_ffmpeg)) },
				captionContent = { Text(stringResource(R.string.prefer_exoplayer_ffmpeg_content)) },
				trailingContent = { Checkbox(checked = preferExoPlayerFfmpeg) },
				onClick = { preferExoPlayerFfmpeg = !preferExoPlayerFfmpeg },
			)
		}

		if (!isTvDevice) {
			item {
				val systemPreferences = koinInject<org.jellyfin.androidtv.preference.SystemPreferences>()
				var disableUiModeWarning by rememberPreference(
					systemPreferences,
					org.jellyfin.androidtv.preference.SystemPreferences.disableUiModeWarning,
				)
				ListButton(
					headingContent = { Text(stringResource(R.string.disable_ui_mode_warning)) },
					trailingContent = { Checkbox(checked = disableUiModeWarning) },
					onClick = { disableUiModeWarning = !disableUiModeWarning },
				)
			}
		}

		item {
			val imageLoader = koinInject<ImageLoader>()
			var imageCacheSize by remember { mutableLongStateOf(imageLoader.diskCache?.size ?: 0L) }
			ListButton(
				headingContent = { Text(stringResource(R.string.clear_image_cache)) },
				captionContent = {
					Text(stringResource(R.string.clear_image_cache_content, Formatter.formatFileSize(context, imageCacheSize)))
				},
				onClick = {
					imageLoader.memoryCache?.clear()
					imageLoader.diskCache?.clear()
					imageCacheSize = imageLoader.diskCache?.size ?: 0L
				},
			)
		}

		item {
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_reset_all_settings)) },
				captionContent = { Text(stringResource(R.string.pref_reset_all_settings_description)) },
				onClick = { showResetDialog = true },
			)
		}
	}

	// ── Dialogs ──

	val currentUpdateInfo = updateInfoForDialog
	if (currentUpdateInfo != null && !showReleaseNotes) {
		UpdateAvailableDialog(
			updateInfo = currentUpdateInfo,
			onDownload = {
				updateInfoForDialog = null
				downloadAndInstall(context, updateChecker, currentUpdateInfo)
			},
			onReleaseNotes = { showReleaseNotes = true },
			onDismiss = { updateInfoForDialog = null },
		)
	}

	if (currentUpdateInfo != null && showReleaseNotes) {
		ReleaseNotesDialog(
			updateInfo = currentUpdateInfo,
			onDownload = {
				showReleaseNotes = false
				updateInfoForDialog = null
				downloadAndInstall(context, updateChecker, currentUpdateInfo)
			},
			onViewOnGitHub = { openUrl(context, currentUpdateInfo.releaseUrl) },
			onDismiss = {
				showReleaseNotes = false
				updateInfoForDialog = null
			},
		)
	}

	if (showResetDialog) {
		ResetSettingsDialog(
			onConfirm = {
				PreferenceManager
					.getDefaultSharedPreferences(context)
					.edit()
					.clear()
					.apply()
				showResetDialog = false
				Toast.makeText(context, R.string.pref_reset_all_settings, Toast.LENGTH_SHORT).show()
			},
			onDismiss = { showResetDialog = false },
		)
	}
}
