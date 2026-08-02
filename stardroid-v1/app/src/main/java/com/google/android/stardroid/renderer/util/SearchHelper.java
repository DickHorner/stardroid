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

package com.google.android.stardroid.renderer.util;

import com.google.android.stardroid.math.MathUtils;
import com.google.android.stardroid.math.Matrix4x4;
import com.google.android.stardroid.math.Vector3;

public class SearchHelper {
  static final float CENTERED_ENTER_DEGREES = 0.10f;
  static final float CENTERED_EXIT_DEGREES = 0.15f;
  private static final float TARGET_SCREEN_MARGIN = 0.92f;

  public void resize(int width, int height) {
    // Kept as part of the renderer lifecycle. Screen-space guidance uses normalized
    // device coordinates and therefore needs no cached pixel dimensions here.
  }

  public void setTarget(Vector3 target, String targetName) {
    mTargetName = targetName;
    mTarget = target.normalizedCopy();
    mTransformedPosition = null;
    mWasInFocusLastCheck = false;
  }

  public void setTransform(Matrix4x4 transformMatrix) {
    mTransformMatrix = transformMatrix;
    mTransformedPosition = null;
  }

  public void setLookDirection(Vector3 lookDirection) {
    mLookDirection = lookDirection.normalizedCopy();
  }

  public Vector3 getTransformedPosition() {
    if (mTransformedPosition == null && mTransformMatrix != null) {
      mTransformedPosition = Matrix4x4.transformVector(mTransformMatrix, mTarget);
    }
    return mTransformedPosition;
  }

  public boolean targetInFocusRadius() {
    return mWasInFocusLastCheck;
  }

  public boolean isTargetOnScreen() {
    Vector3 position = getTransformedPosition();
    return position != null
        && position.z > 0
        && MathUtils.abs(position.x) <= TARGET_SCREEN_MARGIN
        && MathUtils.abs(position.y) <= TARGET_SCREEN_MARGIN;
  }

  public float getAngularSeparationDegrees() {
    float dot = mTarget.dot(mLookDirection);
    dot = Math.max(-1.0f, Math.min(1.0f, dot));
    return (float) Math.toDegrees(MathUtils.acos(dot));
  }

  // Retained for callers that still model the old animated focus transition.
  public float getTransitionFactor() {
    return mWasInFocusLastCheck ? 1.0f : 0.0f;
  }

  public void checkState() {
    float separationDegrees = getAngularSeparationDegrees();
    if (mWasInFocusLastCheck) {
      mWasInFocusLastCheck = separationDegrees <= CENTERED_EXIT_DEGREES;
    } else {
      mWasInFocusLastCheck = separationDegrees <= CENTERED_ENTER_DEGREES;
    }
  }

  public String getTargetName() {
    return mTargetName;
  }

  private Vector3 mTarget = new Vector3(0, 0, 1);
  private Vector3 mLookDirection = new Vector3(0, 0, 1);
  private Vector3 mTransformedPosition = null;
  private Matrix4x4 mTransformMatrix = null;
  private boolean mWasInFocusLastCheck = false;
  private String mTargetName = "Default target name";
}
