package org.jellyfin.androidtv.util.apiclient

import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType

/**
 * Extension functions for BaseItemDto to simplify common image access patterns.
 * These functions reduce boilerplate when accessing item images with fallbacks.
 */

/**
 * Get the primary image for this item.
 * @return The primary image, or null if not available
 */
fun BaseItemDto.getPrimaryImage() = itemImages[ImageType.PRIMARY]

/**
 * Get the logo image for this item, with fallback to parent logo.
 * @return The logo image from the item or its parent, or null if neither exists
 */
fun BaseItemDto.getLogoImage() = itemImages[ImageType.LOGO] ?: parentImages[ImageType.LOGO]

/**
 * Get the thumb image for this item.
 * @return The thumb image, or null if not available
 */
fun BaseItemDto.getThumbImage() = itemImages[ImageType.THUMB]

/**
 * Get the backdrop image for this item.
 * @return The backdrop image, or null if not available
 */
fun BaseItemDto.getBackdropImage() = itemImages[ImageType.BACKDROP]

/**
 * Get the banner image for this item.
 * @return The banner image, or null if not available
 */
fun BaseItemDto.getBannerImage() = itemImages[ImageType.BANNER]

/**
 * Get the primary image with fallback to parent's primary image.
 * Useful for episodes that might use series artwork.
 * @return The primary image from the item or its parent, or null if neither exists
 */
fun BaseItemDto.getPrimaryImageWithFallback() = itemImages[ImageType.PRIMARY] ?: parentImages[ImageType.PRIMARY]

/**
 * Get the thumb image with fallback to parent's thumb image.
 * Useful for episodes that might use series thumbnails.
 * @return The thumb image from the item or its parent, or null if neither exists
 */
fun BaseItemDto.getThumbImageWithFallback() = itemImages[ImageType.THUMB] ?: parentImages[ImageType.THUMB]

/**
 * Get the best landscape image URL for card display.
 * Priority: THUMB → BACKDROP → parent THUMB → parent BACKDROP → PRIMARY → parent PRIMARY.
 * @param api The API client to build the URL
 * @param fillWidth Requested image width in pixels (default 440)
 * @param quality JPEG quality (default 96)
 * @return The image URL, or null if no image is available
 */
fun BaseItemDto.getCardImageUrl(
	api: ApiClient,
	fillWidth: Int = 440,
	quality: Int = 96,
): String? {
	itemImages[ImageType.THUMB]?.let { return it.getUrl(api, fillWidth = fillWidth, quality = quality) }
	itemBackdropImages.firstOrNull()?.let { return it.getUrl(api, fillWidth = fillWidth, quality = quality) }
	parentImages[ImageType.THUMB]?.let { return it.getUrl(api, fillWidth = fillWidth, quality = quality) }
	parentBackdropImages.firstOrNull()?.let { return it.getUrl(api, fillWidth = fillWidth, quality = quality) }
	itemImages[ImageType.PRIMARY]?.let { return it.getUrl(api, fillWidth = fillWidth, quality = quality) }
	parentImages[ImageType.PRIMARY]?.let { return it.getUrl(api, fillWidth = fillWidth, quality = quality) }
	return null
}
