package ru.timestop.watchdog.telegram.bot;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendVideo;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.timestop.watchdog.properties.TelegramBotProperties;
import ru.timestop.watchdog.properties.WebCameraProperties;
import ru.timestop.watchdog.webcam.WebcamAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by timestop on 06.06.17.
 */
@Component
public class AlarmBotImpl implements AlarmBot, CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmBotImpl.class);

    @Autowired
    private TelegramBotProperties telegramBotProperties;

    @Autowired
    private WebCameraProperties webCameraProperties;

    @Autowired
    private WebcamAgent webcam;

    private TelegramBot bot;

    private Set<Long> chatIds = new CopyOnWriteArraySet<>();

    @Override
    public void run(String... strings) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi();

        ApiContextInitializer.init();
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        /*CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(PROXY_HOST, PROXY_PORT),
                new UsernamePasswordCredentials(PROXY_USER, PROXY_PASSWORD));*/

        HttpHost httpHost = new HttpHost(telegramBotProperties.getProxyHost(), telegramBotProperties.getProxyPort());

        RequestConfig requestConfig = RequestConfig.custom()
                .setProxy(httpHost)
                .setAuthenticationEnabled(false)
                .build();

        botOptions.setRequestConfig(requestConfig);
        botOptions.setHttpProxy(httpHost);
        LOG.info("Create telegram bot");
        bot = new TelegramBot(botOptions);
        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            LOG.error("", e);
        }
    }

    @Override
    public void sendMessage(String message) {
        try {
            for (Long chatId : chatIds) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(telegramBotProperties.getChatId());
                sendMessage.setText(message);
                sendMessage.setChatId(chatId);
                bot.sendMessage(sendMessage);
            }
        } catch (Exception e) {
            LOG.error("Something happend when message try send", e);
        }
    }

    @Override
    public void sendVideo(File file) {
        try {
            for (Long chatId : chatIds) {
                SendVideo video = new SendVideo();
                video.setChatId(telegramBotProperties.getChatId());
                video.setCaption("Video");
                video.setHeight(webCameraProperties.getResolution().getSize().height);
                video.setWidth(webCameraProperties.getResolution().getSize().width);
                video.setNewVideo(file);
                video.setChatId(chatId);
                bot.sendVideo(video);
            }
        } catch (TelegramApiException e) {
            LOG.error("Something happend when vodeo try send", e);
        }
    }

    @Override
    public void sendSnapshot(File file) {
        try {
            for (Long chatId : chatIds) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(telegramBotProperties.getChatId());
                photo.setCaption("Snapshot");
                photo.setNewPhoto(file);
                photo.setChatId(chatId);
                bot.sendPhoto(photo);
            }
        } catch (TelegramApiException e) {
            LOG.error("Something happend when snapshot try send", e);
        }
    }

    private class TelegramBot extends AbilityBot {

        protected TelegramBot(DefaultBotOptions botOptions) {
            super(telegramBotProperties.getBotToken(), telegramBotProperties.getBotName(), botOptions);
            LOG.info("telegram bot initialized");
        }

        @Override
        public int creatorId() {
            return 0;
        }

        @Override
        public void onUpdateReceived(Update update) {
            try {
                if (update.hasMessage()) {
                    Message message = update.getMessage();
                    String userName = update.getMessage().getFrom().getUserName();
                    LOG.info("user name = " + userName);
                    for (String t : telegramBotProperties.getOwners()) {
                        LOG.info(t);
                    }
                    if (telegramBotProperties.getOwners().contains(userName)) {
                        Long chatId = update.getMessage().getChatId();
                        LOG.info("user name = " + userName);
                        LOG.info("Chat id = " + chatId);
                        chatIds.add(chatId);
                        if (message.isCommand()) {
                            switch (message.getText()) {
                                case "/start":
                                    sendKeboard(update);
                                    break;
                                case "/run":
                                    webcam.start();
                                    break;
                                case "/stop":
                                    webcam.stop();
                                    break;
                                case "/doSnapshot":
                                    webcam.doSnapshot();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } else {
                    LOG.info("Not have message...");
                }
            } catch (Exception e) {
                LOG.error("Some error when update", e);
            }
        }

        @Override
        public String getBotUsername() {
            return telegramBotProperties.getBotName();
        }

        @Override
        public String getBotToken() {
            return telegramBotProperties.getBotToken();
        }


        /**
         * @param update
         * @throws TelegramApiException
         */
        private void sendKeboard(Update update) throws TelegramApiException {
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            keyboardFirstRow.add("/run");
            keyboardFirstRow.add("/doSnapshot");
            keyboardFirstRow.add("/stop");
            keyboard.add(keyboardFirstRow);
            replyKeyboardMarkup.setKeyboard(keyboard);

            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Update keyboard");
            this.sendMessage(sendMessage);
        }
    }
}
