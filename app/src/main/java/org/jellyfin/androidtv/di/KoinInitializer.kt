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
		val t0 = System.currentTimeMillis()
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
		val t1 = System.currentTimeMillis()
		timber.log.Timber
			.tag("VFX_PERF")
			.i("VFX_PERF KoinInitializer startKoin: ${t1 - t0}ms")

		// Initialize singletons that need Koin dependencies
		TvManager.systemPreferences = koinApp.koin.get<SystemPreferences>()
		ItemLauncherHelper.defaultApi = koinApp.koin.get<ApiClient>()
		ItemLauncherHelper.apiClientFactory = koinApp.koin.get<ApiClientFactory>()
		val t2 = System.currentTimeMillis()
		timber.log.Timber
			.tag("VFX_PERF")
			.i("VFX_PERF KoinInitializer singletons: ${t2 - t1}ms, total: ${t2 - t0}ms")

		return koinApp
	}

	override fun dependencies() = listOf(LogInitializer::class.java)
}
