package ru.timestop.watchdog.webcam.video.timer;

public interface TimersObserver {
    void timeExpire();

    void fail(Runnable process, Exception event);
}
