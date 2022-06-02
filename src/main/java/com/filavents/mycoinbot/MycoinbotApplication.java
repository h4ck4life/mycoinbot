package com.filavents.mycoinbot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MycoinbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MycoinbotApplication.class, args);

        // Create your bot passing the token received from @BotFather
        TelegramBot bot = new TelegramBot(System.getenv("TELEGRAM_BOT_TOKEN"));

        // Register for updates
        bot.setUpdatesListener(updates -> {
            // ... process updates
            updates.forEach(update -> {
                System.out.println(update.message());
                long chatId = update.message().chat().id();
                SendResponse response = bot.execute(new SendMessage(chatId, "Hello! " + update.message().from().firstName()));
            });
            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

    }

}
