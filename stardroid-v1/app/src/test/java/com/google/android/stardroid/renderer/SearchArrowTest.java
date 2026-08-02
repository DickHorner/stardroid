package com.google.android.stardroid.renderer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SearchArrowTest {
  private static final float DELTA = 0.001f;

  @Test
  public void rightwardMovementUsesRightScreenEdgeAndOutwardAngle() {
    SearchArrow.EdgeMarker marker = SearchArrow.computeEdgeMarker(
        1.0f, 0.0f, 500.0f, 800.0f, 50.0f);

    assertEquals(450.0f, marker.x, DELTA);
    assertEquals(0.0f, marker.y, DELTA);
    assertEquals(0.0f, marker.angleDegrees, DELTA);
  }

  @Test
  public void upwardMovementUsesTopScreenEdgeAndOutwardAngle() {
    SearchArrow.EdgeMarker marker = SearchArrow.computeEdgeMarker(
        0.0f, 1.0f, 500.0f, 800.0f, 50.0f);

    assertEquals(0.0f, marker.x, DELTA);
    assertEquals(750.0f, marker.y, DELTA);
    assertEquals(90.0f, marker.angleDegrees, DELTA);
  }

  @Test
  public void diagonalMovementIntersectsFirstScreenEdge() {
    SearchArrow.EdgeMarker marker = SearchArrow.computeEdgeMarker(
        1.0f, 1.0f, 500.0f, 800.0f, 50.0f);

    assertEquals(450.0f, marker.x, DELTA);
    assertEquals(450.0f, marker.y, DELTA);
    assertEquals(45.0f, marker.angleDegrees, DELTA);
  }
}
