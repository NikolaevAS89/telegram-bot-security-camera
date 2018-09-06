package ru.timestop.watchdog.webcam.video.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Tor
 * @version 1.0.0
 * @since 01.09.2018
 */
public class Timer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Timer.class);

    private final Object lock = new Object();

    private AtomicBoolean isArmed = new AtomicBoolean(false);
    private volatile boolean isRunning = true;
    private volatile long expireTime = 0L;

    private final long cooldawnTime;
    private final long checkTime;

    private TimersObserver observer;

    public Timer(TimersObserver timersObserver) {
        this(5000L, 1000L, timersObserver);
    }

    public Timer(long cooldawnTime, long checkTime, TimersObserver timersObserver) {
        this.cooldawnTime = cooldawnTime;
        this.checkTime = checkTime;
        this.observer = timersObserver;
    }

    public void stop() {
        isRunning = false;
        synchronized (lock) {
            LOG.debug("Try stop timer");
            lock.notify();
        }
    }

    public void prolong() {
        LOG.debug("Timer prolong");
        expireTime = System.currentTimeMillis();
        if (!isArmed.get()) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName("MotionCooldownTimer");
            while (isRunning) {
                synchronized (lock) {
                    long time = System.currentTimeMillis() - expireTime;
                    if (time > cooldawnTime) {
                        if (isArmed.getAndSet(false)) {
                            observer.timeExpire();
                        }
                        LOG.debug("Timer wait");
                        lock.notify();
                        lock.wait();
                    } else {
                        lock.notify();
                        isArmed.set(true);
                        LOG.debug("Timer check");
                        lock.wait(checkTime);
                    }
                }
            }
            if (isArmed.get()) {
                observer.timeExpire();
            }
        } catch (InterruptedException e) {
            LOG.warn("Timer ", e);
            observer.fail(this, e);
        }
        LOG.debug("Timer stopped");
    }
}