package org.jellyfin.androidtv.di

import android.content.Context
import androidx.startup.Initializer
import org.jellyfin.androidtv.LogInitializer
import org.jellyfin.androidtv.preference.SystemPreferences
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncherHelper
import org.jellyfin.androidtv.ui.livetv.TvManager
import org.jellyfin.androidtv.util.sdk.ApiClientFactory
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

class KoinInitializer : Initializer<KoinApplication> {
	override fun create(context: Context): KoinApplication {
		val koinApp =
			startKoin {
				androidContext(context)

				modules(
					androidModule,
					appModule,
					authModule,
					playbackModule,
					preferenceModule,
					utilsModule,
				)
			}

		// Initialize singletons that need Koin dependencies
		TvManager.systemPreferences = koinApp.koin.get<SystemPreferences>()
		ItemLauncherHelper.defaultApi = koinApp.koin.get<ApiClient>()
		ItemLauncherHelper.apiClientFactory = koinApp.koin.get<ApiClientFactory>()

		return koinApp
	}

	override fun dependencies() = listOf(LogInitializer::class.java)
}
