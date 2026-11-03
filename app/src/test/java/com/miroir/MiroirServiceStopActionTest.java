package com.miroir;

import android.app.Service;
import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Regression tests for the crash reported as:
 * "Unable to start service MiroirService with Intent { act=ACTION_STOP ... }"
 *
 * The service used to call startForeground() unconditionally at the top of
 * onStartCommand(), even when the delivered intent was ACTION_STOP. That call can
 * throw on real devices (e.g. ForegroundServiceStartNotAllowedException) when the
 * process was relaunched from scratch just to deliver the notification's "Stop"
 * action after the service had already died. Handling ACTION_STOP must never
 * attempt to (re-)promote the service to the foreground.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MiroirServiceStopActionTest {

    @Test
    public void stopActionOnFreshlyCreatedServiceDoesNotStartForeground() {
        Intent stopIntent = new Intent(MiroirService.ACTION_STOP);

        ServiceController<MiroirService> controller =
            org.robolectric.Robolectric.buildService(MiroirService.class, stopIntent);
        MiroirService service = controller.create().get();

        int result = service.onStartCommand(stopIntent, 0, 1);

        ShadowService shadowService = Shadows.shadowOf(service);

        assertNull("ACTION_STOP must not (re-)promote the service to foreground",
            shadowService.getLastForegroundNotification());
        assertEquals(Service.START_NOT_STICKY, result);
    }

    @Test
    public void stopActionOnAlreadyRunningServiceStopsWithoutRepostingNotification() {
        Intent startIntent = new Intent(MiroirService.ACTION_START);
        ServiceController<MiroirService> controller =
            org.robolectric.Robolectric.buildService(MiroirService.class, startIntent);
        MiroirService service = controller.create().get();

        // A denied/cancelled capture permission short-circuits before spawning capture
        // threads, so this reaches the shared foreground-notification code path safely.
        service.onStartCommand(startIntent, 0, 1);
        ShadowService shadowService = Shadows.shadowOf(service);
        Object firstNotification = shadowService.getLastForegroundNotification();

        Intent stopIntent = new Intent(MiroirService.ACTION_STOP);
        service.onStartCommand(stopIntent, 0, 2);

        // The notification object recorded by the shadow must be exactly the one
        // from the ACTION_START call: ACTION_STOP must not have called
        // startForeground() again.
        assertEquals(firstNotification, shadowService.getLastForegroundNotification());
    }

    @Test
    public void nullIntentDoesNotThrowAndStopsSelf() {
        ServiceController<MiroirService> controller =
            org.robolectric.Robolectric.buildService(MiroirService.class);
        MiroirService service = controller.create().get();

        int result = service.onStartCommand(null, 0, 1);

        assertEquals(Service.START_NOT_STICKY, result);
    }
}
