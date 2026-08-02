// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.stardroid.renderer;

import android.content.res.Resources;

import com.google.android.stardroid.R;
import com.google.android.stardroid.math.MathUtils;
import com.google.android.stardroid.math.Vector3;
import com.google.android.stardroid.renderer.util.SearchHelper;
import com.google.android.stardroid.renderer.util.TextureManager;
import com.google.android.stardroid.renderer.util.TextureReference;
import com.google.android.stardroid.renderer.util.TexturedQuad;

import javax.microedition.khronos.opengles.GL10;

public class SearchArrow {
  private static final float ARROW_SIZE = 0.11f;
  private static final float MIN_DIRECTION_LENGTH = 0.000001f;

  private Vector3 mTarget = new Vector3(0, 0, 1);
  private TexturedQuad mArrowQuad = null;
  private float mArrowSizePixels = 1;
  private float mHalfWidth = 1;
  private float mHalfHeight = 1;
  private float mEdgeInset = 1;
  private float mLastDirectionX = 1;
  private float mLastDirectionY = 0;

  private TextureReference mArrowTex = null;

  public void reloadTextures(GL10 gl, Resources res, TextureManager textureManager) {
    gl.glEnable(GL10.GL_TEXTURE_2D);
    mArrowTex = textureManager.getTextureFromResource(gl, R.drawable.arrow);
    gl.glDisable(GL10.GL_TEXTURE_2D);
  }

  public void resize(GL10 gl, int screenWidth, int screenHeight) {
    float shortSide = Math.min(screenWidth, screenHeight);
    mArrowSizePixels = ARROW_SIZE * shortSide;
    mHalfWidth = screenWidth * 0.5f;
    mHalfHeight = screenHeight * 0.5f;
    mEdgeInset = mArrowSizePixels * 0.65f + 8.0f;
    mArrowQuad = new TexturedQuad(mArrowTex,
                                  0, 0, 0,
                                  0.5f, 0, 0,
                                  0, 0.5f, 0);
  }

  public void draw(GL10 gl, Vector3 lookDir, Vector3 upDir, SearchHelper searchHelper,
                   boolean nightVisionMode) {
    if (searchHelper.isTargetOnScreen()) {
      return;
    }

    Vector3 rightDir = lookDir.times(upDir).normalizedCopy();
    float directionX = mTarget.dot(rightDir);
    float directionY = mTarget.dot(upDir);
    float directionLength = MathUtils.sqrt(
        directionX * directionX + directionY * directionY);
    if (directionLength > MIN_DIRECTION_LENGTH) {
      mLastDirectionX = directionX / directionLength;
      mLastDirectionY = directionY / directionLength;
    }

    EdgeMarker marker = computeEdgeMarker(
        mLastDirectionX,
        mLastDirectionY,
        mHalfWidth,
        mHalfHeight,
        mEdgeInset);

    gl.glEnable(GL10.GL_BLEND);
    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND);
    gl.glTexEnvfv(
        GL10.GL_TEXTURE_ENV,
        GL10.GL_TEXTURE_ENV_COLOR,
        nightVisionMode
            ? new float[] {0.8f, 0.0f, 0.0f, 0.0f}
            : new float[] {1.0f, 0.55f, 0.0f, 0.0f},
        0);
    gl.glColor4f(1.0f, 1.0f, 1.0f, 0.9f);

    gl.glPushMatrix();
    // Overlay coordinates are inverted on both axes. Negating the screen-space
    // position and rotating the right-pointing texture by 180 degrees preserves
    // the intended on-screen movement direction.
    gl.glTranslatef(-marker.x, -marker.y, 0);
    gl.glRotatef(marker.angleDegrees + 180.0f, 0, 0, 1);
    gl.glScalef(mArrowSizePixels, mArrowSizePixels, mArrowSizePixels);
    mArrowQuad.draw(gl);
    gl.glPopMatrix();

    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
    gl.glDisable(GL10.GL_BLEND);
  }

  public void setTarget(Vector3 position) {
    mTarget = position.normalizedCopy();
  }

  static EdgeMarker computeEdgeMarker(float directionX, float directionY,
                                      float halfWidth, float halfHeight, float inset) {
    float length = MathUtils.sqrt(directionX * directionX + directionY * directionY);
    if (length < MIN_DIRECTION_LENGTH) {
      directionX = 1.0f;
      directionY = 0.0f;
    } else {
      directionX /= length;
      directionY /= length;
    }

    float maxX = Math.max(1.0f, halfWidth - inset);
    float maxY = Math.max(1.0f, halfHeight - inset);
    float scaleX = MathUtils.abs(directionX) < MIN_DIRECTION_LENGTH
        ? Float.POSITIVE_INFINITY
        : maxX / MathUtils.abs(directionX);
    float scaleY = MathUtils.abs(directionY) < MIN_DIRECTION_LENGTH
        ? Float.POSITIVE_INFINITY
        : maxY / MathUtils.abs(directionY);
    float scale = Math.min(scaleX, scaleY);

    return new EdgeMarker(
        directionX * scale,
        directionY * scale,
        (float) Math.toDegrees(MathUtils.atan2(directionY, directionX)));
  }

  static final class EdgeMarker {
    final float x;
    final float y;
    final float angleDegrees;

    EdgeMarker(float x, float y, float angleDegrees) {
      this.x = x;
      this.y = y;
      this.angleDegrees = angleDegrees;
    }
  }
}
