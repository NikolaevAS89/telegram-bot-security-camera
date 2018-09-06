package ru.timestop.watchdog.telegram.bot;

import java.io.File;

public interface AlarmBot {

    void sendMessage(String message);

    void sendVideo(File file);

    void sendSnapshot(File file);
}
