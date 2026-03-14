package org.jellyfin.androidtv.ui.base.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FiberSmartRecord
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized icon registry for VegafoX.
 * Maps all UI icons to Material Symbols (via material-icons-extended).
 *
 * Custom drawables NOT included here (keep as XML resources):
 * - ic_vegafox, ic_vegafox_fox (VegafoX logo)
 * - ic_jellyfin (Jellyfin logo + notification icon)
 * - ic_jellyseerr_jellyfish, ic_seer (Jellyseerr/Overseerr logos)
 * - ic_rt_fresh, ic_rt_rotten (Rotten Tomatoes)
 * - qr_code, qr_jellyfin_docs (QR codes)
 * - seasonal_* (seasonal animations)
 */
object VegafoXIcons {
	// ── Navigation ──────────────────────────────────────────────
	val Home: ImageVector = Icons.Default.Home
	val Search: ImageVector = Icons.Default.Search
	val Movie: ImageVector = Icons.Default.Movie
	val Tv: ImageVector = Icons.Default.Tv
	val LiveTv: ImageVector = Icons.Default.LiveTv
	val MusicLibrary: ImageVector = Icons.Default.LibraryMusic
	val Settings: ImageVector = Icons.Default.Settings
	val VideoLibrary: ImageVector = Icons.Default.VideoLibrary

	// ── Playback ────────────────────────────────────────────────
	val Play: ImageVector = Icons.Default.PlayArrow
	val Pause: ImageVector = Icons.Default.Pause
	val FastForward: ImageVector = Icons.Default.FastForward
	val Rewind: ImageVector = Icons.Default.FastRewind
	val SkipNext: ImageVector = Icons.Default.SkipNext
	val SkipPrevious: ImageVector = Icons.Default.SkipPrevious
	val Loop: ImageVector = Icons.Default.Loop
	val Shuffle: ImageVector = Icons.Default.Shuffle
	val Speed: ImageVector = Icons.Default.Speed
	val Subtitles: ImageVector = Icons.Default.Subtitles
	val Audiotrack: ImageVector = Icons.Default.Audiotrack
	val HighQuality: ImageVector = Icons.Default.HighQuality
	val Chapter: ImageVector = Icons.Default.Bookmark
	val AspectRatio: ImageVector = Icons.Default.AspectRatio
	val Replay10: ImageVector = Icons.Default.Replay10
	val Forward30: ImageVector = Icons.Default.Forward30
	val Hd: ImageVector = Icons.Default.Hd
	val ZoomOutMap: ImageVector = Icons.Default.ZoomOutMap
	val ListBulleted: ImageVector = Icons.Default.FormatListBulleted
	val AudioSync: ImageVector = Icons.Default.Sync
	val SubtitleSync: ImageVector = Icons.Default.Sync
	val ChannelBar: ImageVector = Icons.Default.LiveTv
	val CastList: ImageVector = Icons.Default.Cast

	// ── Actions ─────────────────────────────────────────────────
	val Favorite: ImageVector = Icons.Default.Favorite
	val FavoriteOutlined: ImageVector = Icons.Default.FavoriteBorder
	val Add: ImageVector = Icons.Default.Add
	val Delete: ImageVector = Icons.Default.Delete
	val Edit: ImageVector = Icons.Default.Edit
	val Refresh: ImageVector = Icons.Default.Refresh
	val Filter: ImageVector = Icons.Default.FilterList
	val Sort: ImageVector = Icons.AutoMirrored.Default.Sort
	val Check: ImageVector = Icons.Default.Check
	val Close: ImageVector = Icons.Default.Close
	val Logout: ImageVector = Icons.AutoMirrored.Default.Logout
	val Upload: ImageVector = Icons.Default.Upload
	val Download: ImageVector = Icons.Default.Download

	// ── Media info ──────────────────────────────────────────────
	val Star: ImageVector = Icons.Default.Star
	val Schedule: ImageVector = Icons.Default.Schedule
	val Visibility: ImageVector = Icons.Default.Visibility
	val Album: ImageVector = Icons.Default.Album

	// ── Recording ───────────────────────────────────────────────
	val Record: ImageVector = Icons.Default.FiberManualRecord
	val RecordSeries: ImageVector = Icons.Default.FiberSmartRecord

	// ── User ────────────────────────────────────────────────────
	val Person: ImageVector = Icons.Default.Person
	val PersonAdd: ImageVector = Icons.Default.PersonAdd
	val Group: ImageVector = Icons.Default.Group
	val Lock: ImageVector = Icons.Default.Lock

	// ── UI / Navigation chrome ──────────────────────────────────
	val ArrowBack: ImageVector = Icons.AutoMirrored.Default.ArrowBack
	val ArrowForward: ImageVector = Icons.AutoMirrored.Default.ArrowForward
	val ExpandLess: ImageVector = Icons.Default.ExpandLess
	val ExpandMore: ImageVector = Icons.Default.ExpandMore
	val KeyboardArrowUp: ImageVector = Icons.Default.KeyboardArrowUp
	val KeyboardArrowDown: ImageVector = Icons.Default.KeyboardArrowDown
	val MoreHoriz: ImageVector = Icons.Default.MoreHoriz
	val Error: ImageVector = Icons.Default.Error
	val Info: ImageVector = Icons.Default.Info
	val Help: ImageVector = Icons.AutoMirrored.Default.Help
	val Folder: ImageVector = Icons.Default.Folder

	// ── Settings / Feature-specific ─────────────────────────────
	val Tune: ImageVector = Icons.Default.Tune
	val Science: ImageVector = Icons.Default.Science
	val GridView: ImageVector = Icons.Default.GridView
	val Guide: ImageVector = Icons.Default.MenuBook
	val Lightbulb: ImageVector = Icons.Default.Lightbulb
	val Clapperboard: ImageVector = Icons.Default.MovieCreation
	val Genres: ImageVector = Icons.Default.TheaterComedy
	val SyncPlay: ImageVector = Icons.Default.Groups
	val Microphone: ImageVector = Icons.Default.Mic
	val Mix: ImageVector = Icons.Default.AutoAwesome
	val Trailer: ImageVector = Icons.Default.Videocam
	val TvPlay: ImageVector = Icons.Default.SmartDisplay
	val TvTimer: ImageVector = Icons.Default.Timer
	val NextUp: ImageVector = Icons.Default.Upcoming
	val Sleep: ImageVector = Icons.Default.Bedtime
	val Abc: ImageVector = Icons.Default.SortByAlpha
	val HomeEdit: ImageVector = Icons.Default.EditLocation
	val PhotoLibrary: ImageVector = Icons.Default.PhotoLibrary
	val Photo: ImageVector = Icons.Default.Photo
	val Calendar: ImageVector = Icons.Default.CalendarToday
	val Artist: ImageVector = Icons.Default.Person

	// ── Jellyseerr status ───────────────────────────────────────
	val Available: ImageVector = Icons.Default.CheckCircle
	val PartiallyAvailable: ImageVector = Icons.Default.Downloading
	val Declined: ImageVector = Icons.Default.Cancel
	val Pending: ImageVector = Icons.Default.Pending
	val Spinner: ImageVector = Icons.Default.HourglassEmpty
}
