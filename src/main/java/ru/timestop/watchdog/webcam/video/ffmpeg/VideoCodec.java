package ru.timestop.watchdog.webcam.video.ffmpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.timestop.watchdog.webcam.ProcessObserver;

import java.io.File;
import java.io.IOException;

/**
 * @author Tor
 * @version 1.0.0
 * @since 01.09.2018
 */
public class VideoCodec implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(VideoCodec.class);
    private static final String PROCESS_NAME = "Video codec process in ";

    private String[] cmd;
    private String logDirectory;
    private String workDirectory;
    private ProcessObserver observer;

    @Override
    public void run() {
        try {
            Thread.currentThread().setName("VideoCodecJob-" + workDirectory);
            ProcessBuilder processBuilder = new ProcessBuilder()
                    .command(cmd)
                    .directory(new File(workDirectory));
            if (logDirectory != null) {
                File dirLog = new File(logDirectory);
                File fileErr = new File(logDirectory + File.separator + "err.log");
                File fileOut = new File(logDirectory + File.separator + "out.log");
                dirLog.mkdirs();
                processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(fileErr))
                        .redirectOutput(ProcessBuilder.Redirect.appendTo(fileOut));
            }
            LOG.info("start video codec in " + workDirectory);
            processBuilder.start().waitFor();
            String path = workDirectory + File.separator + "output.mp4";
            File p = new File(path);
            if (p.exists()) {
                LOG.info("Video done");
            } else {
                LOG.warn("Video not saved:" + workDirectory);
            }
            observer.doneVideo(new File(path));
        } catch (IOException | InterruptedException e) {
            LOG.error("Something wrong ", e);
            observer.fail(this, e);
        }
    }

    public String toString() {
        return PROCESS_NAME + " " + workDirectory;
    }

    private void setCmd(String[] cmd) {
        this.cmd = cmd;
    }

    private void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
    }

    private void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
    }

    private void setObserver(ProcessObserver observer) {
        this.observer = observer;
    }

    public static class Factory {
        private final String[] cmd;
        private String logDirectory = null;
        private ProcessObserver observer = null;

        public Factory(String codecPath, int fps) {
            this.cmd = new String[]{codecPath, "-f", "image2", "-r", String.valueOf(fps), "-i", "s%d.jpg", "-vcodec", "libx264", "output.mp4"};
        }

        public Factory setLogDirectory(String logDirectory) {
            this.logDirectory = logDirectory;
            return this;
        }

        public Factory setObserver(ProcessObserver observer) {
            this.observer = observer;
            return this;
        }

        public VideoCodec build(String workDirectory) {
            VideoCodec codec = new VideoCodec();
            codec.setCmd(cmd);
            codec.setLogDirectory(logDirectory);
            codec.setObserver(observer);
            codec.setWorkDirectory(workDirectory);
            return codec;
        }
    }
}