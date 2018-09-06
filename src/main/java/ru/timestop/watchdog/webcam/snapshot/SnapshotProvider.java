package ru.timestop.watchdog.webcam.snapshot;

import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.timestop.watchdog.properties.ApplicationProperties;
import ru.timestop.watchdog.webcam.ProcessObserver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Tor
 * @version 1.0.0
 * @since 30.08.2018
 */
@Component
public class SnapshotProvider implements WebcamListener {
    private static final Logger LOG = LoggerFactory.getLogger(SnapshotProvider.class);
    private static final String PROCESS_NAME = "Snapshot provider";

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isSnapshot = new AtomicBoolean(false);

    @Autowired
    @Qualifier("WebcamAgentSingle")
    private ProcessObserver observer;

    @Autowired
    private ApplicationProperties applicationProperties;

    private Job job = null;

    private ExecutorService executor = Executors.newSingleThreadExecutor();


    public void start() {
        if (!isRunning.getAndSet(true)) {
            job = new Job();
            executor.execute(job);
        }
    }

    public void stop() {
        if (isRunning.getAndSet(false)) {
            job.stop();
            job = null;
        }
    }

    public void doSnapshot() {
        if (isRunning.get()) {
            isSnapshot.set(true);
            LOG.debug("do snapshot");
        }
    }

    @Override
    public void webcamImageObtained(WebcamEvent webcamEvent) {
        if (isSnapshot.getAndSet(false)) {
            job.addSnapshot(webcamEvent.getImage());
        }
    }

    @Override
    public void webcamOpen(WebcamEvent webcamEvent) {
        //SKIP
    }

    @Override
    public void webcamClosed(WebcamEvent webcamEvent) {
        //SKIP
    }

    @Override
    public void webcamDisposed(WebcamEvent webcamEvent) {
        //SKIP
    }

    @Override
    public String toString() {
        return PROCESS_NAME;
    }

    /**
     * @param snapshot
     */
    private void fireSnapshotDone(File snapshot) {
        observer.doneSnapshot(snapshot);
    }

    /**
     *
     */
    private class Job implements Runnable {
        private final LinkedList<BufferedImage> snapshots = new LinkedList<>();

        private volatile boolean isRunning = true;

        synchronized void stop() {
            isRunning = false;
            this.notify();
        }

        synchronized void addSnapshot(BufferedImage image) {
            snapshots.addFirst(image);
            this.notify();
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().setName("SaveSnapshotJob");
                while (isRunning || !snapshots.isEmpty()) {
                    synchronized (this) {
                        if (snapshots.isEmpty()) {
                            if (isRunning) {
                                this.notify();
                                this.wait();
                            }
                        } else {
                            BufferedImage image = snapshots.removeFirst();
                            this.notify();
                            String path = applicationProperties.getSnapshotDirectory() + File.separator + UUID.randomUUID().toString() + ".jpg";
                            File snapshot = new File(path);
                            ImageIO.write(image, "JPG", snapshot);
                            fireSnapshotDone(snapshot);
                        }
                    }
                }
            } catch (InterruptedException | IOException e) {
                observer.fail(this, e);
                LOG.warn("Snapshot ", e);
            }
            LOG.debug("snapshot manager stopped");
        }

    }
}
