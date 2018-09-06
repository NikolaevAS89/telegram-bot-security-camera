package ru.timestop.watchdog.webcam.video;

import com.github.sarxos.webcam.WebcamMotionEvent;

import java.awt.image.BufferedImage;

/**
 * @author Tor
 * @version 1.0.0
 * @since 01.09.2018
 */
public class DetectedMotion {

    private final long time;
    private final String filePath;
    private final BufferedImage image;

    public DetectedMotion(String filePath, BufferedImage image) {
        this.image = image;
        this.time = System.currentTimeMillis();
        this.filePath = filePath;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getTime() {
        return time;
    }
}
