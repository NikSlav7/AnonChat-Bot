package com.example.anaonchatbot;

import com.example.anaonchatbot.telegram.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class AnaonchatbotApplication {

	public static void main(String[] args) throws TelegramApiException {
		ApplicationContext context = SpringApplication.run(AnaonchatbotApplication.class, args);
		TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
		TelegramBot bot = context.getBean(TelegramBot.class);
		api.registerBot(bot);
	}

}
