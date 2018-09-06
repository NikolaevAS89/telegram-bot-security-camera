package ru.timestop.watchdog.properties;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 13.09.2018
 */
public interface ApplicationProperties {

    String getLogDirectory();

    String getSnapshotDirectory();

    String getVideoDirectory();

    String getMpegVideoCodecPath();

    int getCodecFPS();
}
