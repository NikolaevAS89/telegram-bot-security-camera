package ru.timestop.watchdog.webcam;

/**
 *
 */
public interface WebcamAgent {
    /**
     *
     */
    void start();

    /**
     *
     */
    void stop();

    /**
     *
     */
    void doSnapshot();
}
