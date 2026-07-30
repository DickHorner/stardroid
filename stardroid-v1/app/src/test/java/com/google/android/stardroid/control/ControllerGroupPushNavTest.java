package com.google.android.stardroid.control;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.os.Handler;

import com.google.android.stardroid.ApplicationConstants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ControllerGroupPushNavTest {
  @Test
  public void configuredPushNavDisablesPhoneOrientationSources() {
    SensorOrientationController sensorController = mock(SensorOrientationController.class);
    FakePushNavOrientationController pushNavController = configuredPushNavController();
    LocationController locationController = mock(LocationController.class);
    AstronomerModel model = mock(AstronomerModel.class);

    ControllerGroup controllerGroup = new ControllerGroup(
        sensorController, pushNavController, locationController);
    controllerGroup.setModel(model);
    clearInvocations(sensorController, model);

    controllerGroup.start();

    assertTrue(controllerGroup.isPushNavMode());
    assertTrue(pushNavController.enabled);
    verify(sensorController).setEnabled(false);
    verify(model).setAutoUpdatePointing(false);
  }

  @Test
  public void autoManualToggleDoesNotOverrideConfiguredPushNav() {
    SensorOrientationController sensorController = mock(SensorOrientationController.class);
    FakePushNavOrientationController pushNavController = configuredPushNavController();
    LocationController locationController = mock(LocationController.class);
    AstronomerModel model = mock(AstronomerModel.class);

    ControllerGroup controllerGroup = new ControllerGroup(
        sensorController, pushNavController, locationController);
    controllerGroup.setModel(model);
    controllerGroup.start();
    clearInvocations(sensorController, model);

    controllerGroup.setAutoMode(false);

    assertTrue(pushNavController.enabled);
    verify(sensorController).setEnabled(false);
    verify(model).setAutoUpdatePointing(false);
  }

  private static FakePushNavOrientationController configuredPushNavController() {
    SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    when(sharedPreferences.getString(
        ApplicationConstants.PUSHNAV_SERVER_URL_PREF_KEY, ""))
        .thenReturn("http://pushnav.local:8765");
    return new FakePushNavOrientationController(sharedPreferences, mock(Handler.class));
  }

  private static final class FakePushNavOrientationController
      extends PushNavOrientationController {
    private boolean enabled;

    FakePushNavOrientationController(
        SharedPreferences sharedPreferences, Handler handler) {
      super(sharedPreferences, handler);
    }

    @Override
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    @Override
    public void start() {
      // No network connection in this controller-selection test.
    }

    @Override
    public void stop() {
      // No network connection in this controller-selection test.
    }
  }
}
