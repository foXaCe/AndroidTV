package org.jellyfin.androidtv.di

import org.jellyfin.androidtv.data.service.pluginsync.PluginSyncService
import org.jellyfin.androidtv.preference.LiveTvPreferences
import org.jellyfin.androidtv.preference.PreferencesRepository
import org.jellyfin.androidtv.preference.SystemPreferences
import org.jellyfin.androidtv.preference.TelemetryPreferences
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferenceModule = module {
	single { PluginSyncService(androidContext(), get(), get(), get(), get(), get(), get()) }
	single { PreferencesRepository(get(), get(), get(), get()) }

	single { LiveTvPreferences(get()) }
	factory { UserSettingPreferences(get()) }
	factory { UserPreferences(get()) }
	factory { SystemPreferences(get()) }
	factory { TelemetryPreferences(get()) }
}
