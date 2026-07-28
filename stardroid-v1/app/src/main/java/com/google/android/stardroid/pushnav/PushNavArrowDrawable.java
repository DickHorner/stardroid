/*
 * Copyright (c) 2026 Jasper Luetkens.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 */
package com.google.android.stardroid.pushnav;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/** Screen-fixed arrow where 0 degrees points up and 90 degrees points right. */
final class PushNavArrowDrawable extends Drawable {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path path = new Path();
  private float angleDeg;

  PushNavArrowDrawable(int color, float strokeWidthPx) {
    paint.setColor(color);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(strokeWidthPx);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeJoin(Paint.Join.ROUND);
  }

  void setAngleDeg(float angleDeg) {
    this.angleDeg = normalizeAngle(angleDeg);
    invalidateSelf();
  }

  static float normalizeAngle(float angleDeg) {
    float normalized = angleDeg % 360.0f;
    return normalized < 0.0f ? normalized + 360.0f : normalized;
  }

  @Override
  public void draw(Canvas canvas) {
    int saveCount = canvas.save();
    float centerX = getBounds().exactCenterX();
    float centerY = getBounds().exactCenterY();
    float half = Math.min(getBounds().width(), getBounds().height()) * 0.38f;

    canvas.rotate(angleDeg, centerX, centerY);
    path.reset();
    path.moveTo(centerX, centerY + half);
    path.lineTo(centerX, centerY - half);
    path.moveTo(centerX, centerY - half);
    path.lineTo(centerX - half * 0.45f, centerY - half * 0.45f);
    path.moveTo(centerX, centerY - half);
    path.lineTo(centerX + half * 0.45f, centerY - half * 0.45f);
    canvas.drawPath(path, paint);
    canvas.restoreToCount(saveCount);
  }

  @Override
  public void setAlpha(int alpha) {
    paint.setAlpha(alpha);
    invalidateSelf();
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
}
