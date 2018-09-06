package ru.timestop.watchdog.webcam;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.timestop.watchdog.properties.WebCameraProperties;
import ru.timestop.watchdog.telegram.bot.AlarmBot;
import ru.timestop.watchdog.webcam.snapshot.SnapshotProvider;
import ru.timestop.watchdog.webcam.video.MotionRecorded;

import java.io.File;

/**
 * @author Tor
 * @version 1.0.0
 * @since 27.08.2018
 */
@Component
@Qualifier("WebcamAgentSingle")
public class WebcamAgentSingle implements WebcamAgent, ProcessObserver {

    @Autowired
    private WebCameraProperties webCameraProperties;

    @Autowired
    private AlarmBot alarmBot;

    @Autowired
    private SnapshotProvider snapshotProvider;

    @Autowired
    private MotionRecorded videoRecorder;

    private boolean isRuning = false;

    private Webcam webcam;
    private WebcamMotionDetector detector;

    @Override
    public void start() {
        if (!isRuning) {
            webcam = Webcam.getDefault();
            webcam.getDevice().setResolution(webCameraProperties.getResolution().getSize());

            int interval = 1000 / webCameraProperties.getFPS();

            detector = new WebcamMotionDetector(webcam);
            detector.setInterval(interval);
            detector.setAreaThreshold(webCameraProperties.getAreaThreshold());

            snapshotProvider.start();
            videoRecorder.start();

            webcam.addWebcamListener(snapshotProvider);
            webcam.addWebcamListener(videoRecorder);
            detector.addMotionListener(videoRecorder);

            detector.start();
            isRuning = true;
        }
    }

    @Override
    public void stop() {
        if (isRuning) {
            detector.stop();
            videoRecorder.stop();
            snapshotProvider.stop();

            webcam.removeWebcamListener(snapshotProvider);
            webcam.removeWebcamListener(videoRecorder);
            detector.removeMotionListener(videoRecorder);

            webcam = null;
            detector = null;
            isRuning = false;
        }
    }

    @Override
    public void doSnapshot() {
        snapshotProvider.doSnapshot();
    }


    @Override
    public void motionDetected() {
        alarmBot.sendMessage("Motion detected");
    }

    @Override
    public void doneSnapshot(File result) {
        alarmBot.sendSnapshot(result);
    }

    @Override
    public void doneVideo(File result) {
        alarmBot.sendVideo(result);
    }

    @Override
    public void fail(Runnable process, Exception event) {
        alarmBot.sendMessage(process.toString() + " is fail: " + event.getMessage());
        restart();
    }

    /**
     *
     */
    private void restart() {
        stop();
        start();
    }
}
