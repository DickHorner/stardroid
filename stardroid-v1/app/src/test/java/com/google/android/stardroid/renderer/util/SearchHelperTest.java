package com.google.android.stardroid.renderer.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.android.stardroid.math.Matrix4x4;
import com.google.android.stardroid.math.Vector3;

import org.junit.Test;

public class SearchHelperTest {
  @Test
  public void centeredUsesTenthDegreeThreshold() {
    SearchHelper helper = new SearchHelper();
    helper.setTarget(new Vector3(1, 0, 0), "target");

    helper.setLookDirection(vectorAtAngleDegrees(0.09));
    helper.checkState();
    assertTrue(helper.targetInFocusRadius());

    helper.setLookDirection(vectorAtAngleDegrees(0.16));
    helper.checkState();
    assertFalse(helper.targetInFocusRadius());
  }

  @Test
  public void centeredStateHasSmallExitHysteresis() {
    SearchHelper helper = new SearchHelper();
    helper.setTarget(new Vector3(1, 0, 0), "target");

    helper.setLookDirection(vectorAtAngleDegrees(0.09));
    helper.checkState();
    assertTrue(helper.targetInFocusRadius());

    helper.setLookDirection(vectorAtAngleDegrees(0.14));
    helper.checkState();
    assertTrue(helper.targetInFocusRadius());

    helper.setLookDirection(vectorAtAngleDegrees(0.16));
    helper.checkState();
    assertFalse(helper.targetInFocusRadius());
  }

  @Test
  public void targetOnScreenRequiresFrontFacingPositionInsideMargins() {
    SearchHelper helper = new SearchHelper();
    helper.setTransform(Matrix4x4.createIdentity());

    helper.setTarget(new Vector3(0.2f, 0.1f, 1.0f), "visible");
    assertTrue(helper.isTargetOnScreen());

    helper.setTarget(new Vector3(10.0f, 0.0f, 1.0f), "outside");
    assertFalse(helper.isTargetOnScreen());

    helper.setTarget(new Vector3(0.0f, 0.0f, -1.0f), "behind");
    assertFalse(helper.isTargetOnScreen());
  }

  private static Vector3 vectorAtAngleDegrees(double degrees) {
    double radians = Math.toRadians(degrees);
    return new Vector3((float) Math.cos(radians), (float) Math.sin(radians), 0.0f);
  }
}
