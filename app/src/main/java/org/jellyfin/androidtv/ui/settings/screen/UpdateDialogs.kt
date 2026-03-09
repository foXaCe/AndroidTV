package org.jellyfin.androidtv.ui.settings.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.UpdateCheckerService
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.preference.category.GlassDialogButton
import timber.log.Timber

@Composable
internal fun UpdateAvailableDialog(
	updateInfo: UpdateCheckerService.UpdateInfo,
	onDownload: () -> Unit,
	onReleaseNotes: () -> Unit,
	onDismiss: () -> Unit,
) {
	val downloadFocusRequester = remember { FocusRequester() }
	val sizeMB = updateInfo.apkSize / (1024.0 * 1024.0)

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier = Modifier
					.widthIn(min = 340.dp, max = 460.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.dialogScrim)
					.border(1.dp, JellyfinTheme.colorScheme.toolbarDivider, JellyfinTheme.shapes.dialog)
					.padding(vertical = 20.dp),
			) {
				Text(
					text = stringResource(R.string.update_dialog_title),
					style = JellyfinTheme.typography.titleLarge,
					color = JellyfinTheme.colorScheme.onSurface,
					modifier = Modifier
						.padding(horizontal = 24.dp)
						.padding(bottom = 12.dp),
				)

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				Spacer(modifier = Modifier.height(16.dp))

				Text(
					text = stringResource(R.string.update_new_version, updateInfo.version),
					style = JellyfinTheme.typography.titleMedium,
					color = JellyfinTheme.colorScheme.onSurface,
					modifier = Modifier.padding(horizontal = 24.dp),
				)

				Spacer(modifier = Modifier.height(8.dp))

				Text(
					text = stringResource(R.string.update_size, String.format("%.1f", sizeMB)),
					style = JellyfinTheme.typography.bodyMedium,
					color = JellyfinTheme.colorScheme.textHint,
					modifier = Modifier.padding(horizontal = 24.dp),
				)

				Spacer(modifier = Modifier.height(24.dp))

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				Spacer(modifier = Modifier.height(16.dp))

				Column(
					modifier = Modifier.padding(horizontal = 24.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					GlassDialogButton(
						text = stringResource(R.string.btn_download),
						onClick = onDownload,
						isPrimary = true,
						modifier = Modifier.focusRequester(downloadFocusRequester),
					)

					GlassDialogButton(
						text = stringResource(R.string.update_release_notes),
						onClick = onReleaseNotes,
					)

					GlassDialogButton(
						text = stringResource(R.string.btn_later),
						onClick = onDismiss,
					)
				}
			}
		}
	}

	LaunchedEffect(Unit) {
		downloadFocusRequester.requestFocus()
	}
}

@Composable
internal fun ReleaseNotesDialog(
	updateInfo: UpdateCheckerService.UpdateInfo,
	onDownload: () -> Unit,
	onViewOnGitHub: () -> Unit,
	onDismiss: () -> Unit,
) {
	val downloadFocusRequester = remember { FocusRequester() }
	val sizeMB = updateInfo.apkSize / (1024.0 * 1024.0)

	val htmlContent = remember(updateInfo) {
		buildString {
			append("<!DOCTYPE html><html><head>")
			append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
			append("<style>")
			append("body { font-family: sans-serif; padding: 16px; background-color: transparent; color: #e0e0e0; margin: 0; }")
			append("h1, h2, h3 { color: #ffffff; margin-top: 16px; margin-bottom: 8px; }")
			append("h1 { font-size: 1.5em; }")
			append("h2 { font-size: 1.3em; }")
			append("h3 { font-size: 1.1em; }")
			append("p { margin: 8px 0; line-height: 1.5; }")
			append("ul, ol { margin: 8px 0; padding-left: 24px; line-height: 1.6; }")
			append("li { margin: 4px 0; }")
			append("code { background-color: rgba(255,255,255,0.08); padding: 2px 6px; border-radius: 3px; font-family: monospace; color: #f0f0f0; }")
			append("pre { background-color: rgba(255,255,255,0.06); padding: 12px; border-radius: 4px; overflow-x: auto; }")
			append("pre code { background-color: transparent; padding: 0; }")
			append("a { color: #00A4DC; text-decoration: none; }")
			append("blockquote { border-left: 3px solid #00A4DC; margin: 8px 0; padding-left: 12px; color: #b0b0b0; }")
			append("strong { color: #ffffff; }")
			append("hr { border: none; border-top: 1px solid rgba(255,255,255,0.1); margin: 16px 0; }")
			append("</style></head><body>")
			append("<h2>Version ${updateInfo.version}</h2>")
			append("<p><strong>Size:</strong> ${String.format("%.1f", sizeMB)} MB</p>")
			append("<hr>")

			val releaseNotes = updateInfo.releaseNotes
				.replace("### ", "<h3>")
				.replace("## ", "<h2>")
				.replace("# ", "<h1>")
				.replace(Regex("(?<!<h[1-3]>)(.+)"), "$1</p>")
				.replace(Regex("<h([1-3])>(.+?)</p>"), "<h$1>$2</h$1>")
				.replace(Regex("^- (.+)"), "<li>$1</li>")
				.replace(Regex("((?:<li>.*</li>\n?)+)"), "<ul>$1</ul>")
				.replace(Regex("^\\* (.+)"), "<li>$1</li>")
				.replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
				.replace(Regex("`(.+?)`"), "<code>$1</code>")
				.replace("\n\n", "</p><p>")
				.replace(Regex("^(?!<[uh]|<li|<p)(.+)"), "<p>$1")

			append(releaseNotes)
			append("</body></html>")
		}
	}

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier = Modifier
					.widthIn(min = 500.dp, max = 800.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.dialogScrim)
					.border(1.dp, JellyfinTheme.colorScheme.toolbarDivider, JellyfinTheme.shapes.dialog)
					.padding(vertical = 20.dp),
			) {
				Text(
					text = stringResource(R.string.update_release_notes),
					style = JellyfinTheme.typography.titleLarge,
					color = JellyfinTheme.colorScheme.onSurface,
					modifier = Modifier
						.padding(horizontal = 24.dp)
						.padding(bottom = 12.dp),
				)

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				AndroidView(
					factory = { ctx ->
						WebView(ctx).apply {
							layoutParams = LinearLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								(ctx.resources.displayMetrics.heightPixels * 0.55).toInt()
							)
							setBackgroundColor(android.graphics.Color.TRANSPARENT)
							settings.apply {
								javaScriptEnabled = false
								defaultTextEncodingName = "utf-8"
							}
							loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
						}
					},
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 4.dp),
				)

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				Spacer(modifier = Modifier.height(16.dp))

				Column(
					modifier = Modifier.padding(horizontal = 24.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					GlassDialogButton(
						text = stringResource(R.string.btn_download),
						onClick = onDownload,
						isPrimary = true,
						modifier = Modifier.focusRequester(downloadFocusRequester),
					)

					GlassDialogButton(
						text = stringResource(R.string.btn_view_on_github),
						onClick = onViewOnGitHub,
					)

					GlassDialogButton(
						text = stringResource(R.string.btn_close),
						onClick = onDismiss,
					)
				}
			}
		}
	}

	LaunchedEffect(Unit) {
		downloadFocusRequester.requestFocus()
	}
}

internal fun checkForUpdates(
	context: Context,
	updateChecker: UpdateCheckerService,
	onUpdateFound: (UpdateCheckerService.UpdateInfo) -> Unit,
) {
	CoroutineScope(Dispatchers.Main).launch {
		Toast.makeText(context, context.getString(R.string.update_checking), Toast.LENGTH_SHORT).show()

		try {
			val result = updateChecker.checkForUpdate()
			result.fold(
				onSuccess = { updateInfo ->
					if (updateInfo == null) {
						Toast.makeText(context, context.getString(R.string.update_check_failed), Toast.LENGTH_LONG).show()
					} else if (!updateInfo.isNewer) {
						Toast.makeText(context, context.getString(R.string.update_none_available), Toast.LENGTH_LONG).show()
					} else {
						onUpdateFound(updateInfo)
					}
				},
				onFailure = { error ->
					Timber.e(error, "Failed to check for updates")
					Toast.makeText(context, context.getString(R.string.update_check_failed), Toast.LENGTH_LONG).show()
				}
			)
		} catch (e: Exception) {
			Timber.e(e, "Error checking for updates")
			Toast.makeText(context, context.getString(R.string.update_check_error), Toast.LENGTH_LONG).show()
		}
	}
}

internal fun downloadAndInstall(
	context: Context,
	updateChecker: UpdateCheckerService,
	updateInfo: UpdateCheckerService.UpdateInfo
) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		if (!context.packageManager.canRequestPackageInstalls()) {
			androidx.appcompat.app.AlertDialog.Builder(context)
				.setTitle(context.getString(R.string.settings_install_permission_title))
				.setMessage(context.getString(R.string.settings_install_permission_message))
				.setPositiveButton(context.getString(R.string.btn_open_settings)) { _, _ ->
					openInstallPermissionSettings(context)
				}
				.setNegativeButton(context.getString(R.string.btn_cancel), null)
				.show()
			return
		}
	}

	CoroutineScope(Dispatchers.Main).launch {
		Toast.makeText(context, context.getString(R.string.update_downloading), Toast.LENGTH_SHORT).show()

		try {
			val result = updateChecker.downloadUpdate(updateInfo.downloadUrl) { progress ->
				Timber.d("Download progress: $progress%")
			}

			result.fold(
				onSuccess = { apkUri ->
					Toast.makeText(context, context.getString(R.string.update_downloaded), Toast.LENGTH_SHORT).show()
					updateChecker.installUpdate(apkUri)
				},
				onFailure = { error ->
					Timber.e(error, "Failed to download update")
					Toast.makeText(context, context.getString(R.string.update_download_failed), Toast.LENGTH_LONG).show()
				}
			)
		} catch (e: Exception) {
			Timber.e(e, "Error downloading update")
			Toast.makeText(context, context.getString(R.string.update_download_error), Toast.LENGTH_LONG).show()
		}
	}
}

internal fun openUrl(context: Context, url: String) {
	try {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
		context.startActivity(intent)
	} catch (e: Exception) {
		Timber.e(e, "Failed to open URL")
		Toast.makeText(context, context.getString(R.string.update_failed_open_url), Toast.LENGTH_LONG).show()
	}
}

private fun openInstallPermissionSettings(context: Context) {
	try {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
				data = Uri.parse("package:${context.packageName}")
			}
			context.startActivity(intent)
		}
	} catch (e: Exception) {
		Timber.e(e, "Failed to open install permission settings")
		Toast.makeText(context, context.getString(R.string.update_failed_open_settings), Toast.LENGTH_LONG).show()
	}
}
