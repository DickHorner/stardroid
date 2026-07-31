// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.stardroid.renderer;

import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;

import com.google.android.stardroid.ApplicationConstants;
import com.google.android.stardroid.math.RaDec;
import com.google.android.stardroid.math.Vector3;
import com.google.android.stardroid.observing.CurrentSearchTarget;
import com.google.android.stardroid.pushnav.PushNavTargetSender;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

/**
 * Allows the rest of the program to communicate with the SkyRenderer by queueing
 * events.
 * @author James Powell
 */
public class RendererController extends RendererControllerBase {
  @EntryPoint
  @InstallIn(SingletonComponent.class)
  interface PushNavEntryPoint {
    SharedPreferences sharedPreferences();
    ScheduledExecutorService backgroundExecutor();
  }

  /**
   * Used for grouping renderer calls into atomic units.
   */
  public static class AtomicSection extends RendererControllerBase {
    private Queuer mQueuer = new Queuer();
    private static int NEXT_ID = 0;
    private int mID;

    private AtomicSection(SkyRenderer renderer) {
      super(renderer);
      synchronized(AtomicSection.class) {
        mID = NEXT_ID++;
      }
    }

    @Override
    protected EventQueuer getQueuer() {
      return mQueuer;
    }

    @Override
    public String toString() {
      return "AtomicSection" + mID;
    }

    private Queue<Runnable> releaseEvents() {
      Queue<Runnable> queue = mQueuer.mQueue;
      mQueuer = new Queuer();
      return queue;
    }

    private static class Queuer implements EventQueuer {
      private Queue<Runnable> mQueue = new LinkedList<Runnable>();
      public void queueEvent(Runnable r) {
        mQueue.add(r);
      }
    }
  }

  private final EventQueuer mQueuer;
  private final SharedPreferences sharedPreferences;
  private final ScheduledExecutorService backgroundExecutor;

  @Override
  protected EventQueuer getQueuer() {
    return mQueuer;
  }

  public RendererController(SkyRenderer renderer, final GLSurfaceView view) {
    super(renderer);
    mQueuer = view::queueEvent;
    PushNavEntryPoint entryPoint = EntryPointAccessors.fromApplication(
        view.getContext().getApplicationContext(), PushNavEntryPoint.class);
    sharedPreferences = entryPoint.sharedPreferences();
    backgroundExecutor = entryPoint.backgroundExecutor();
  }

  @Override
  public void queueEnableSearchOverlay(final Vector3 target, final String targetName) {
    CurrentSearchTarget.update(targetName);
    RaDec targetRaDec = RaDec.fromGeocentricCoords(target);
    String serverUrl = sharedPreferences.getString(
        ApplicationConstants.PUSHNAV_SERVER_URL_PREF_KEY, "");
    PushNavTargetSender.sendAsync(
        backgroundExecutor, serverUrl, targetRaDec.getRa(), targetRaDec.getDec());
    super.queueEnableSearchOverlay(target, targetName);
  }

  @Override
  public void queueDisableSearchOverlay() {
    CurrentSearchTarget.clear();
    super.queueDisableSearchOverlay();
  }

  @Override
  public String toString() {
    return "RendererController";
  }

  public AtomicSection createAtomic() {
    return new AtomicSection(mRenderer);
  }

  public void queueAtomic(final AtomicSection atomic) {
    String msg = "Applying " + atomic.toString();
    queueRunnable(msg, CommandType.Synchronization, new Runnable() { public void run() {
      Queue<Runnable> events = atomic.releaseEvents();
      for (Runnable r : events) {
        r.run();
      }
    }});
  }

  // Must only be called from within an update closure (GL thread).
  public boolean isSearchTargetInFocus() {
    return mRenderer.isSearchTargetInFocus();
  }
}
