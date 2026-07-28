package com.google.android.stardroid.pushnav

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import kotlin.math.min

internal class PushNavArrowDrawable(
  color: Int,
  strokeWidthPx: Float
) : Drawable() {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    style = Paint.Style.STROKE
    strokeWidth = strokeWidthPx
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
  }
  private val path = Path()
  private var angleDeg = 0.0f

  fun setAngleDeg(angleDeg: Float) {
    this.angleDeg = normalizeAngle(angleDeg)
    invalidateSelf()
  }

  override fun draw(canvas: Canvas) {
    val saveCount = canvas.save()
    val centerX = bounds.exactCenterX()
    val centerY = bounds.exactCenterY()
    val half = min(bounds.width(), bounds.height()) * 0.38f

    canvas.rotate(angleDeg, centerX, centerY)
    path.reset()
    path.moveTo(centerX, centerY + half)
    path.lineTo(centerX, centerY - half)
    path.moveTo(centerX, centerY - half)
    path.lineTo(centerX - half * 0.45f, centerY - half * 0.45f)
    path.moveTo(centerX, centerY - half)
    path.lineTo(centerX + half * 0.45f, centerY - half * 0.45f)
    canvas.drawPath(path, paint)
    canvas.restoreToCount(saveCount)
  }

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
    invalidateSelf()
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    paint.colorFilter = colorFilter
    invalidateSelf()
  }

  @Deprecated("Deprecated in Android")
  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  companion object {
    @JvmStatic
    fun normalizeAngle(angleDeg: Float): Float {
      val normalized = angleDeg % 360.0f
      return if (normalized < 0.0f) normalized + 360.0f else normalized
    }
  }
}
