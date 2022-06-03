package com.filavents.mycoinbot.startup;

import com.filavents.mycoinbot.model.Crypto;
import com.filavents.mycoinbot.model.repository.AlertRepository;
import com.filavents.mycoinbot.service.CryptoService;
import com.filavents.mycoinbot.service.impl.LunoCryptoServiceImpl;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class TelegramBot implements ApplicationRunner {

    @Value("${mycoinbot.lunoEndpointUrl}")
    private String lunoEndpointUrl;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private final CryptoService cryptoService = new LunoCryptoServiceImpl();

    private com.pengrad.telegrambot.TelegramBot bot = null;

    public final String CMD_PRICE = "/price";
    public final String CMD_ALERT = "/alert";
    public final String CMD_LIST = "/list";
    public final String CMD_HELP = "/help";

    @Override
    public void run(ApplicationArguments args) {

        System.out.println("Starting Telegram Bot...");

        // Create your bot passing the token received from @BotFather
        bot = new com.pengrad.telegrambot.TelegramBot(System.getenv("TELEGRAM_BOT_TOKEN"));

        // Register for updates
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                long chatId = update.message().chat().id();
                try {

                    switch (update.message().text()) {
                        case CMD_PRICE:
                            Crypto crypto = cryptoService.getLatestCryptoPrice(
                                    "BTC",
                                    lunoEndpointUrl
                            );
                            String currentPrice = "1 BTC - " + formatCurrency(crypto.getPrice().doubleValue());
                            replyMessage(chatId, currentPrice);
                            break;
                        case CMD_ALERT:
                            replyMessage(chatId, "New price alert saved.");
                            break;
                        case CMD_LIST:
                            replyMessage(chatId, "Active alerts:\n> 132000\n< 125000");
                            break;
                        case CMD_HELP:
                            replyMessage(chatId, "Please contact @h4ck4life for further support. Thanks.");
                            break;
                        default:
                    }
                } catch (Exception ex) {
                    replyMessage(chatId, "Please try again later.");
                    ex.printStackTrace();
                }
            });
            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public String formatCurrency(double price) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));
        return currency.format(price);
    }

    public String[] extractUserCommand(String message) {
        return Arrays.stream(message.split(" ")).limit(2).toArray(String[]::new);
    }

    public String[] extractPriceAlertCommand(String message) {
        return Arrays.stream(message.split(" ")).limit(2).toArray(String[]::new);
    }

    public boolean isWhiteListedPriceAlertCommand(String priceAlertCommand) {
        String[] whitelist = {"<", ">"};
        return Arrays.stream(whitelist).anyMatch(s -> s.equals(priceAlertCommand));
    }

    private void replyMessage(long chatId, String message) {
        SendResponse response = bot.execute(
                new SendMessage(chatId, message)
        );
    }
}
