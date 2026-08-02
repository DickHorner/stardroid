// Copyright 2009 Google Inc.
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

import static com.google.android.stardroid.math.MathUtilsKt.TWO_PI;

import android.content.res.Resources;

import com.google.android.stardroid.R;
import com.google.android.stardroid.math.MathUtils;
import com.google.android.stardroid.math.Vector3;
import com.google.android.stardroid.renderer.util.SearchHelper;
import com.google.android.stardroid.renderer.util.TextureManager;
import com.google.android.stardroid.renderer.util.TextureReference;
import com.google.android.stardroid.renderer.util.TexturedQuad;

import javax.microedition.khronos.opengles.GL10;

public class CrosshairOverlay {
  private static final float TARGET_MARKER_SIZE_PX = 44.0f;
  private static final float CENTER_MARKER_SIZE_PX = 26.0f;

  public void reloadTextures(GL10 gl, Resources res, TextureManager textureManager) {
    mTex = textureManager.getTextureFromResource(gl, R.drawable.crosshair);
  }

  public void resize(GL10 gl, int screenWidth, int screenHeight) {
    mHalfWidth = screenWidth * 0.5f;
    mHalfHeight = screenHeight * 0.5f;
    mTargetQuad = createQuad(TARGET_MARKER_SIZE_PX);
    mCenterQuad = createQuad(CENTER_MARKER_SIZE_PX);
  }

  public void draw(GL10 gl, SearchHelper searchHelper, boolean nightVisionMode) {
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glEnable(GL10.GL_BLEND);
    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);

    drawCenterMarker(gl, nightVisionMode);
    if (searchHelper.isTargetOnScreen()) {
      drawTargetMarker(gl, searchHelper, nightVisionMode);
    }

    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
    gl.glDisable(GL10.GL_BLEND);
    gl.glPopMatrix();
  }

  private void drawCenterMarker(GL10 gl, boolean nightVisionMode) {
    if (nightVisionMode) {
      gl.glColor4f(0.65f, 0.0f, 0.0f, 0.55f);
    } else {
      gl.glColor4f(0.75f, 0.75f, 0.75f, 0.55f);
    }
    mCenterQuad.draw(gl);
  }

  private void drawTargetMarker(GL10 gl, SearchHelper searchHelper, boolean nightVisionMode) {
    Vector3 position = searchHelper.getTransformedPosition();
    if (position == null || position.z <= 0) {
      return;
    }

    int period = 1000;
    long time = System.currentTimeMillis();
    float intensity = 0.7f + 0.3f * MathUtils.sin((time % period) * TWO_PI / period);
    if (nightVisionMode) {
      gl.glColor4f(intensity, 0, 0, 0.85f);
    } else {
      gl.glColor4f(intensity, intensity * 0.75f, 0, 0.85f);
    }

    gl.glPushMatrix();
    // SearchHelper returns normalized device coordinates; the overlay uses pixel units.
    gl.glTranslatef(position.x * mHalfWidth, position.y * mHalfHeight, 0);
    mTargetQuad.draw(gl);
    gl.glPopMatrix();
  }

  private TexturedQuad createQuad(float sizePx) {
    float radius = sizePx * 0.5f;
    return new TexturedQuad(mTex,
                            0, 0, 0,
                            radius, 0, 0,
                            0, radius, 0);
  }

  private float mHalfWidth = 1;
  private float mHalfHeight = 1;
  private TexturedQuad mTargetQuad = null;
  private TexturedQuad mCenterQuad = null;
  private TextureReference mTex = null;
}
