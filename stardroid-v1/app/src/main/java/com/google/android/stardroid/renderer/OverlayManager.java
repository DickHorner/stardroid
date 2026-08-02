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

import android.content.res.Resources;
import android.opengl.GLU;
import android.util.Log;

import com.google.android.stardroid.math.Vector3;
import com.google.android.stardroid.renderer.util.ColoredQuad;
import com.google.android.stardroid.renderer.util.SearchHelper;
import com.google.android.stardroid.renderer.util.TextureManager;

import javax.microedition.khronos.opengles.GL10;

public class OverlayManager extends RendererObjectManager {
  private int mWidth = 2;
  private int mHeight = 2;
  private Vector3 mLookDir = new Vector3(0, 0, 0);
  private Vector3 mUpDir = new Vector3(0, 1, 0);

  private boolean mSearching = false;
  private SearchHelper mSearchHelper = new SearchHelper();
  private ColoredQuad mDarkQuad = null;
  private SearchArrow mSearchArrow = new SearchArrow();
  private CrosshairOverlay mCrosshair = new CrosshairOverlay();

  private TextureManager mTextureManager;

  public OverlayManager(int layer, TextureManager manager) {
    super(layer, manager);
  }

  @Override
  public void reload(GL10 gl, boolean fullReload) {
    Resources res = getRenderState().getResources();
    mSearchArrow.reloadTextures(gl, res, textureManager());
    mCrosshair.reloadTextures(gl, res, textureManager());
  }

  public void resize(GL10 gl, int screenWidth, int screenHeight) {
    mWidth = screenWidth;
    mHeight = screenHeight;

    mSearchHelper.resize(screenWidth, screenHeight);
    mSearchArrow.resize(gl, screenWidth, screenHeight);
    mCrosshair.resize(gl, screenWidth, screenHeight);

    mDarkQuad = new ColoredQuad(0, 0, 0, 0.6f,
                                0, 0, 0,
                                screenWidth, 0, 0,
                                0, screenHeight, 0);
  }

  public void setViewOrientation(Vector3 lookDir, Vector3 upDir) {
    mLookDir = lookDir;
    mUpDir = upDir;
  }

  @Override
  public void drawInternal(GL10 gl) {
    setupMatrices(gl);

    if (mSearching) {
      mSearchHelper.setTransform(getRenderState().getTransformToDeviceMatrix());
      mSearchHelper.setLookDirection(mLookDir);
      mSearchHelper.checkState();

      mDarkQuad.draw(gl);
      mCrosshair.draw(gl, mSearchHelper, getRenderState().getNightVisionMode());
      mSearchArrow.draw(gl, mLookDir, mUpDir, mSearchHelper,
                        getRenderState().getNightVisionMode());
    }

    restoreMatrices(gl);
  }

  public void setViewerUpDirection(Vector3 viewerUp) {
    // Search guidance is projected directly in the current look/up basis.
  }

  public void enableSearchOverlay(Vector3 target, String targetName) {
    Log.d("OverlayManager", "Searching for " + target);
    mSearching = true;
    mSearchHelper.setTransform(getRenderState().getTransformToDeviceMatrix());
    mSearchHelper.setTarget(target, targetName);
    mSearchArrow.setTarget(target);
    queueForReload(false);
  }

  public void disableSearchOverlay() {
    mSearching = false;
  }

  public boolean isSearchTargetInFocus() {
    return mSearching && mSearchHelper.targetInFocusRadius();
  }

  private void setupMatrices(GL10 gl) {
    float halfWidth = mWidth / 2.0f;
    float halfHeight = mHeight / 2.0f;

    gl.glMatrixMode(GL10.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glLoadIdentity();
    GLU.gluOrtho2D(gl, -halfWidth, halfWidth, -halfHeight, halfHeight);

    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glLoadIdentity();
  }

  private void restoreMatrices(GL10 gl) {
    gl.glMatrixMode(GL10.GL_PROJECTION);
    gl.glPopMatrix();

    gl.glMatrixMode(GL10.GL_MODELVIEW);
    gl.glPopMatrix();
  }
}
