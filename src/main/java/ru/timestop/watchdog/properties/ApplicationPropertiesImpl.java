package ru.timestop.watchdog.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 13.09.2018
 */
@Component
public class ApplicationPropertiesImpl implements ApplicationProperties {

    @Value("${telegram.bot.log.directory}")
    private String logDirectory;

    @Value("${telegram.bot.snapshot.directory}")
    private String snapshotDirectory;

    @Value("${telegram.bot.video.directory}")
    private String videoDirectory;

    @Value("${mpeg.codec.path}")
    private String mpegCodecPath;

    @Value("${mpeg.codec.fps}")
    private int codecFPS;

    @Override
    public String getLogDirectory() {
        return logDirectory;
    }

    @Override
    public String getSnapshotDirectory() {
        return snapshotDirectory;
    }

    @Override
    public String getVideoDirectory() {
        return videoDirectory;
    }

    @Override
    public String getMpegVideoCodecPath() {
        return mpegCodecPath;
    }

    @Override
    public int getCodecFPS() {
        return codecFPS;
    }
}
