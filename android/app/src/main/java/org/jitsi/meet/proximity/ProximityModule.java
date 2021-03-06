package org.jitsi.meet.proximity;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;

/**
 * Module implementing a simple API to enable a proximity sensor-controlled
 * wake lock. When the lock is held, if the proximity sensor detects a nearby
 * object it will dim the screen and disable touch controls. The functionality
 * is used with the conference audio-only mode.
 */
public class ProximityModule extends ReactContextBaseJavaModule {
    /**
     * React Native module name.
     */
    private static final String MODULE_NAME = "Proximity";

    /**
     * This type of wake lock (the one activated by the proximity sensor) has
     * been available for a while, but the constant was only exported in API
     * level 21 (Android Marshmallow) so make no assumptions and use its value
     * directly.
     *
     * TODO: Remove when we bump the API level to 21.
     */
    private static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;

    /**
     * {@link WakeLock} instance.
     */
    private final WakeLock wakeLock;

    /**
     * Initializes a new module instance. There shall be a single instance of
     * this module throughout the lifetime of the application.
     *
     * @param reactContext The {@link ReactApplicationContext} where this module
     * is created.
     */
    public ProximityModule(ReactApplicationContext reactContext) {
        super(reactContext);

        WakeLock wakeLock;
        PowerManager powerManager
            = (PowerManager)
                reactContext.getSystemService(Context.POWER_SERVICE);

        try {
            wakeLock
                = powerManager.newWakeLock(
                        PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                        MODULE_NAME);
        } catch (Throwable ignored) {
            wakeLock = null;
        }

        this.wakeLock = wakeLock;
    }

    /**
     * Gets the name of this module to be used in the React Native bridge.
     *
     * @return The name of this module to be used in the React Native bridge.
     */
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    /**
     * Acquires / releases the proximity sensor wake lock.
     *
     * @param enabled {@code true} to enable the proximity sensor; otherwise,
     * {@code false}.
     */
    @ReactMethod
    public void setEnabled(final boolean enabled) {
        if (wakeLock == null) {
            return;
        }

        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enabled) {
                    if (!wakeLock.isHeld()) {
                        wakeLock.acquire();
                    }
                } else if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        });
    }
}
