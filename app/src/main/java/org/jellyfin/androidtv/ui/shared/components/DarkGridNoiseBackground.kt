package org.jellyfin.androidtv.ui.shared.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import kotlin.random.Random

private val GridBaseColor = Color(0xFF060A0F)
private val RadialCenterColor = Color(0xFF0A1525)
private const val NOISE_SEED = 42
private const val NOISE_POINT_COUNT = 4000

/**
 * Dark Grid Noise background — trading/terminal dashboard style.
 * Layers: solid base, fractal noise, geometric grid, radial glow, vignette.
 * Uses drawWithCache — all positions and brushes are cached until size changes.
 */
@Composable
fun DarkGridNoiseBackground(modifier: Modifier = Modifier) {
	val density = LocalDensity.current
	val gridStepPx = with(density) { 60.dp.toPx() }
	val dotSizePx = with(density) { 1.dp.toPx() }
	val lineWidthPx = with(density) { 1.dp.toPx() }
	val gridColor = VegafoXColors.OrangePrimary.copy(alpha = 0.06f)

	Box(
		modifier =
			modifier
				.fillMaxSize()
				.drawWithCache {
					val w = size.width
					val h = size.height
					val cx = w / 2f
					val cy = h / 2f

					// Pre-compute noise positions (cached until size changes)
					val rng = Random(NOISE_SEED)
					val noisePoints =
						Array(NOISE_POINT_COUNT) {
							Triple(rng.nextFloat() * w, rng.nextFloat() * h, rng.nextFloat() * 0.05f)
						}

					// Pre-compute radial gradient brush
					val radialBrush =
						Brush.radialGradient(
							colors = listOf(RadialCenterColor, Color.Transparent),
							center = Offset(cx, cy),
							radius = w * 0.35f,
						)
					val radiusX = w * 0.35f
					val scaleY = (h * 0.25f) / radiusX

					// Pre-compute vignette dimensions & brushes
					val vw = w * 0.15f
					val vh = h * 0.15f
					val vAlpha = 0.7f
					val vwLeft = w * 0.05f
					val leftBrush =
						Brush.horizontalGradient(
							colors = listOf(Color.Black.copy(alpha = vAlpha * 0.5f), Color.Transparent),
							startX = 0f,
							endX = vwLeft,
						)
					val rightBrush =
						Brush.horizontalGradient(
							colors = listOf(Color.Transparent, Color.Black.copy(alpha = vAlpha)),
							startX = w - vw,
							endX = w,
						)
					val topBrush =
						Brush.verticalGradient(
							colors = listOf(Color.Black.copy(alpha = vAlpha), Color.Transparent),
							startY = 0f,
							endY = vh,
						)
					val bottomBrush =
						Brush.verticalGradient(
							colors = listOf(Color.Transparent, Color.Black.copy(alpha = vAlpha)),
							startY = h - vh,
							endY = h,
						)

					onDrawBehind {
						// Layer 1: Solid base
						drawRect(color = GridBaseColor)

						// Layer 2: Fractal noise — 4000 deterministic random dots
						for ((x, y, a) in noisePoints) {
							drawRect(
								color = Color.White.copy(alpha = a),
								topLeft = Offset(x, y),
								size = Size(dotSizePx, dotSizePx),
							)
						}

						// Layer 3: Geometric grid — OrangePrimary lines every 60dp
						var gx = 0f
						while (gx <= w) {
							drawLine(color = gridColor, start = Offset(gx, 0f), end = Offset(gx, h), strokeWidth = lineWidthPx)
							gx += gridStepPx
						}
						var gy = 0f
						while (gy <= h) {
							drawLine(color = gridColor, start = Offset(0f, gy), end = Offset(w, gy), strokeWidth = lineWidthPx)
							gy += gridStepPx
						}

						// Layer 4: Elliptical radial gradient — center glow
						drawContext.canvas.nativeCanvas.save()
						drawContext.canvas.nativeCanvas.scale(1f, scaleY, cx, cy)
						drawRect(brush = radialBrush, alpha = 0.6f)
						drawContext.canvas.nativeCanvas.restore()

						// Layer 5: Vignette — dark fading edges
						drawRect(brush = leftBrush, size = Size(vwLeft, h))
						drawRect(brush = rightBrush, topLeft = Offset(w - vw, 0f), size = Size(vw, h))
						drawRect(brush = topBrush, size = Size(w, vh))
						drawRect(brush = bottomBrush, topLeft = Offset(0f, h - vh), size = Size(w, vh))
					}
				},
	)
}
