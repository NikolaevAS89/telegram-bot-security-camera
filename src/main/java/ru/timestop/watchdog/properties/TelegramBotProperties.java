package ru.timestop.watchdog.properties;

import java.util.List;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 13.09.2018
 */
public interface TelegramBotProperties {
    String getBotToken();

    String getBotName();

    long getChatId();

    int getProxyPort();

    String getProxyHost();

    String getProxyUser();

    String getProxyPassword();

    List<String> getOwners();
}
