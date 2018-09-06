package ru.timestop.watchdog.webcam.video.motion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.timestop.watchdog.webcam.video.DetectedMotion;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author Tor
 * @version 1.0.0
 * @since 01.09.2018
 */
public class MotionSaver implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MotionSaver.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

    private volatile boolean isRuning = true;

    private LinkedList<DetectedMotion> motionEvents = new LinkedList<>();

    public synchronized void addSnapshot(DetectedMotion event) {
        motionEvents.addFirst(event);
        notify();
    }

    public synchronized void stop() {
        isRuning = false;
        notify();
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName("SaveMotionJob");
            while (isRuning || !motionEvents.isEmpty()) {
                synchronized (this) {
                    if (motionEvents.isEmpty()) {
                        if (isRuning) {
                            notify();
                            wait();
                        }
                    } else {
                        try {
                            DetectedMotion event = motionEvents.removeFirst();
                            notify();
                            BufferedImage image = event.getImage();
                            String text = sdf.format(new Date(event.getTime()));
                            Graphics2D graphics2D = image.createGraphics();
                            graphics2D.setColor(Color.RED);
                            graphics2D.drawString(text, 10, 20);
                            graphics2D.dispose();
                            ImageIO.write(image, "JPG", new File(event.getFilePath()));
                        } catch (IOException e) {
                            LOG.error("Snapshot not save", e);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Snapshot ", e);
        }
        LOG.debug("video manager stopped");
    }
}