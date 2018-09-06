package ru.timestop.watchdog.webcam;

import java.io.File;

public interface ProcessObserver {

    void motionDetected();

    void doneSnapshot(File result);

    void doneVideo(File result);

    void fail(Runnable process, Exception event);
}
