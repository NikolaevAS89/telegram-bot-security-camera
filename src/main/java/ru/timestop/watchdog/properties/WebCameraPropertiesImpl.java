package ru.timestop.watchdog.properties;

import com.github.sarxos.webcam.WebcamResolution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 13.09.2018
 */
@Component
public class WebCameraPropertiesImpl implements WebCameraProperties {

    @Value("${webcamera.fps:10}")
    private int fps;

    @Value("${webcamera.detector.check.interval:100}")
    private int detectorCheckInterval;

    @Value("${webcamera.area.threshold:0.01}")
    private double areaThreshold;

    @Value("${webcamera.resolution:SXGA}")
    private WebcamResolution webcamResolution;

    @Value("${webcamera.check.interval:3000}")
    private long timerCheckInterval;

    @Value("${webcamera.cooldown:5000}")
    private long timerCooldown;

    @Override
    public int getFPS() {
        return fps;
    }

    @Override
    public double getAreaThreshold() {
        return areaThreshold;
    }

    @Override
    public int getDetectorCheckInterval() {
        return detectorCheckInterval;
    }

    @Override
    public WebcamResolution getResolution() {
        return webcamResolution;
    }

    @Override
    public long getTimerCheckInterval() {
        return timerCheckInterval;
    }

    @Override
    public long getTimerCooldown() {
        return timerCooldown;
    }
}
