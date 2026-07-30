package com.google.android.stardroid.control;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class ControllerGroupPushNavTest {
  @Test
  public void configuredPushNavDisablesPhoneOrientationSources() {
    SensorOrientationController sensorController = mock(SensorOrientationController.class);
    PushNavOrientationController pushNavController = mock(PushNavOrientationController.class);
    LocationController locationController = mock(LocationController.class);
    AstronomerModel model = mock(AstronomerModel.class);
    when(pushNavController.isConfigured()).thenReturn(true);

    ControllerGroup controllerGroup = new ControllerGroup(
        sensorController, pushNavController, locationController);
    controllerGroup.setModel(model);
    clearInvocations(sensorController, pushNavController, model);

    controllerGroup.start();

    assertTrue(controllerGroup.isPushNavMode());
    verify(sensorController).setEnabled(false);
    verify(pushNavController).setEnabled(true);
    verify(model).setAutoUpdatePointing(false);
  }

  @Test
  public void autoManualToggleDoesNotOverrideConfiguredPushNav() {
    SensorOrientationController sensorController = mock(SensorOrientationController.class);
    PushNavOrientationController pushNavController = mock(PushNavOrientationController.class);
    LocationController locationController = mock(LocationController.class);
    AstronomerModel model = mock(AstronomerModel.class);
    when(pushNavController.isConfigured()).thenReturn(true);

    ControllerGroup controllerGroup = new ControllerGroup(
        sensorController, pushNavController, locationController);
    controllerGroup.setModel(model);
    controllerGroup.start();
    clearInvocations(sensorController, pushNavController, model);

    controllerGroup.setAutoMode(false);

    verify(sensorController).setEnabled(false);
    verify(pushNavController).setEnabled(true);
    verify(model).setAutoUpdatePointing(false);
  }
}
