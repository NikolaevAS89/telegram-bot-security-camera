package ru.timestop.watchdog.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 13.09.2018
 */
@Component
public class TelegramBotPropertiesImpl implements TelegramBotProperties {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${telegram.bot.chat.id}")
    private long chatId;

    @Value("${telegram.proxy.port:8080}")
    private int proxyPort;

    @Value("${telegram.proxy.host}")
    private String proxyHost;

    @Value("${telegram.proxy.user}")
    private String proxyUser;

    @Value("${telegram.proxy.password}")
    private String proxyPassword;

    @Value("${telegram.bot.owner}")
    private String owners;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotName() {
        return botName;
    }

    @Override
    public long getChatId() {
        return chatId;
    }

    @Override
    public int getProxyPort() {
        return proxyPort;
    }

    @Override
    public String getProxyHost() {
        return proxyHost;
    }

    @Override
    public String getProxyUser() {
        return proxyUser;
    }

    @Override
    public String getProxyPassword() {
        return proxyPassword;
    }

    @Override
    public List<String> getOwners() {
        return Arrays.asList(owners.split(","));
    }
}
