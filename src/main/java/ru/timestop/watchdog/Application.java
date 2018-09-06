package ru.timestop.watchdog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by timestop on 04.06.17.
 */
@SpringBootApplication
public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        try {
            SpringApplication.run(ApplicationConfig.class, args);
        } catch (Exception e) {
            LOG.error("Application fail", e);
        }
    }
}
