package ru.acuma.k.shuffler.config;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.acuma.k.shuffler.bot.BasicBot;

@Slf4j
@Configuration
public class BasicBotConfig {

    @Value("${telegram.bot.name}")
    private String BOT_NAME;

    @Value("${telegram.bot.token}")
    private String BOT_TOKEN;

    @Bean
    public BasicBot botInit() {
        return new BasicBot(BOT_NAME, BOT_TOKEN);
    }

    @Bean
    @SneakyThrows
    public void connect() {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(botInit());
        } catch (TelegramApiException e) {
            log.error("Cannot initialize bot ", e);
        }
    }


}
