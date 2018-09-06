package ru.timestop.watchdog.webcam.video;

import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.timestop.watchdog.properties.ApplicationProperties;
import ru.timestop.watchdog.properties.WebCameraProperties;
import ru.timestop.watchdog.webcam.ProcessObserver;
import ru.timestop.watchdog.webcam.video.ffmpeg.VideoCodec;
import ru.timestop.watchdog.webcam.video.motion.MotionSaver;
import ru.timestop.watchdog.webcam.video.timer.Timer;
import ru.timestop.watchdog.webcam.video.timer.TimersObserver;

import javax.annotation.PostConstruct;
import java.io.File;
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
public class MotionRecorded implements WebcamMotionListener, WebcamListener, TimersObserver {
    private static final Logger LOG = LoggerFactory.getLogger(MotionRecorded.class);

    private volatile boolean isRunning = false;
    private AtomicBoolean isNotified = new AtomicBoolean(false);
    private volatile boolean isRecording = false;
    private int snpCnt = 0;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private WebCameraProperties webCameraProperties;

    @Autowired
    @Qualifier("WebcamAgentSingle")
    private ProcessObserver observer;

    private Timer timer;
    private MotionSaver snapshotSaver;
    private String snapshotsDirectory;

    private VideoCodec.Factory factory;

    private ExecutorService executor = Executors.newFixedThreadPool(3);

    private long lastTime = 0L;

    @PostConstruct
    public void init() {
        this.factory = new VideoCodec.Factory(applicationProperties.getMpegVideoCodecPath(), applicationProperties.getCodecFPS())
                .setLogDirectory(applicationProperties.getLogDirectory())
                .setObserver(observer);

    }

    public synchronized void start() {
        if (!isRunning) {
            snapshotsDirectory = createNewDirectory();

            snapshotSaver = new MotionSaver();
            timer = new Timer(webCameraProperties.getTimerCooldown(), webCameraProperties.getTimerCheckInterval(), this);

            executor.execute(snapshotSaver);
            executor.execute(timer);
            LOG.debug("motion manager started");
            isRunning = true;
        }
    }

    public synchronized void stop() {
        if (isRunning) {
            isRunning = false;
            isRecording = false;
            snapshotSaver.stop();
            timer.stop();
            snapshotSaver = null;
            timer = null;
        }
    }

    @Override
    public void motionDetected(WebcamMotionEvent event) {
        if (isRunning) {
            isRecording = true;
            if (!isNotified.getAndSet(true)) {
                observer.motionDetected();
            }
        }

    }

    @Override
    public synchronized void timeExpire() {
        isNotified.set(false);
        isRecording = false;
        executor.execute(factory.build(snapshotsDirectory));
        snapshotsDirectory = createNewDirectory();
    }


    @Override
    public void fail(Runnable process, Exception e) {
        observer.fail(process, e);
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
    public synchronized void webcamImageObtained(WebcamEvent webcamEvent) {
        if (isRunning && isRecording) {
            if (LOG.isDebugEnabled()) {
                long delta = System.currentTimeMillis() - lastTime;
                LOG.debug("Motion detector lag:" + delta);
                lastTime = System.currentTimeMillis();
            }
            String path = snapshotsDirectory + File.separator + "s" + (++snpCnt) + ".jpg";
            timer.prolong();
            snapshotSaver.addSnapshot(new DetectedMotion(path, webcamEvent.getImage()));
        }
    }

    /**
     * @return created directory
     */
    private String createNewDirectory() {
        snpCnt = 0;
        String directory = applicationProperties.getVideoDirectory() + File.separator + UUID.randomUUID().toString();
        (new File(directory)).mkdirs();
        return directory;
    }
}
