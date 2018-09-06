package ru.timestop.watchdog.properties;

import com.github.sarxos.webcam.WebcamResolution;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 13.09.2018
 */
public interface WebCameraProperties {
    int getFPS();

    int getDetectorCheckInterval();

    double getAreaThreshold();

    WebcamResolution getResolution();

    long getTimerCheckInterval();

    long getTimerCooldown();
}
