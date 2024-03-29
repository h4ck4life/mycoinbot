package com.filavents.mycoinbot.startup;

import com.filavents.mycoinbot.model.Alert;
import com.filavents.mycoinbot.model.Crypto;
import com.filavents.mycoinbot.model.repository.AlertRepository;
import com.filavents.mycoinbot.service.CryptoService;
import com.filavents.mycoinbot.service.impl.LunoCryptoServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Component
public class TelegramBot implements ApplicationRunner {

    Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    @Value("${mycoinbot.lunoEndpointUrl}")
    private String lunoEndpointUrl;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private final CryptoService cryptoService = new LunoCryptoServiceImpl();

    private com.pengrad.telegrambot.TelegramBot bot = null;

    public static Cache<String, Crypto> cache = null;
    public static final String CACHE_KEY_BTCPRICE = "BTCPRICE";

    public static final String CMD_PRICE = "/price";
    public static final String CMD_ALERT = "/alert";
    public static final String CMD_LIST = "/list";
    public static final String CMD_HELP = "/help";

    public TelegramBot() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1)
                .build();
    }

    @Override
    public void run(ApplicationArguments args) {

        logger.info("Starting Telegram Bot...");

        // Create your bot passing the token received from @BotFather
        bot = new com.pengrad.telegrambot.TelegramBot(System.getenv("TELEGRAM_BOT_TOKEN"));

        // Register for updates
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                if (null != update.message()) {
                    long chatId = update.message().chat().id();
                    try {
                        String[] userInputCommand = extractUserCommand(update.message().text());
                        switch (userInputCommand[0]) {
                            case CMD_PRICE:
                                replyLatestBTCMYRPrice(chatId);
                                break;
                            case CMD_ALERT:
                                if(saveNewPriceAlert(userInputCommand[1], chatId)) {
                                    replyActiveAlertList(userInputCommand[1], chatId);
                                }
                                break;
                            case CMD_LIST:
                                String command = userInputCommand.length > 1 ? userInputCommand[1] : null;
                                replyActiveAlertList(command, chatId);
                                break;
                            case CMD_HELP:
                                String helpMsg = "Check current BTC price:" +
                                        "\n" +
                                        "/price" +
                                        "\n" +
                                        "\n" +
                                        "Save new alert:" +
                                        "\n" +
                                        "/alert > 120000" +
                                        "\n" +
                                        "/alert < 120000" +
                                        "\n" +
                                        "\n" +
                                        "View alert list:" +
                                        "\n" +
                                        "/list" +
                                        "\n" +
                                        "\n" +
                                        "Clear alert list:" +
                                        "\n" +
                                        "/list clear";
                                replyMessage(chatId, helpMsg);
                                break;
                            default:
                                replyMessage(chatId, "ℹ️ Please use correct command");
                                break;
                        }
                    } catch (Exception ex) {
                        replyMessage(chatId, "ℹ️ Please use correct command or try again later");
                        ex.printStackTrace();
                    }
                }
            });
            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void replyActiveAlertList(String command, long chatId) {

        if(!StringUtil.isNullOrEmpty(command) && command.trim().equalsIgnoreCase("clear")) {
            alertRepository.clearAllAlertsByChatId(chatId);
        }

        List<Alert> activeAlerts = alertRepository.findAllActiveAlertsByChatId(chatId);
        if (!activeAlerts.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\uD83C\uDFC1 Active alerts:\n");
            //activeAlerts.forEach(alert -> sb.append("[").append(alert.getId()).append("] ").append(alert.getTriggerCondition()).append(" ").append(formatCurrency(alert.getPrice().doubleValue())).append("\n"));
            activeAlerts.forEach(alert -> sb.append(alert.getTriggerCondition()).append(" ").append(formatCurrency(alert.getPrice().doubleValue())).append("\n"));
            replyMessage(chatId, sb.toString());
        } else {
            replyMessage(chatId, "ℹ️ No active alerts");
        }
    }

    private void replyLatestBTCMYRPrice(long chatId) throws Exception {

        Crypto currentBTCPrice = cache.getIfPresent(CACHE_KEY_BTCPRICE);

        if(null == currentBTCPrice) {
            logger.info("Get live price for user chatId: " + chatId);
            currentBTCPrice = cryptoService.getLatestCryptoPrice(
                    "BTC",
                    lunoEndpointUrl
            );
            logger.info("=== Luno Price ===");
            logger.info(currentBTCPrice.toString());
            cache.put(CACHE_KEY_BTCPRICE, currentBTCPrice);
        } else {
            logger.info("Get price from cache for user chatId: " + chatId);
        }

        String currentPrice = "ℹ️ 1 BTC ≈ " + formatCurrency(currentBTCPrice.getPrice().doubleValue());
        replyMessage(chatId, currentPrice);
    }

    private boolean saveNewPriceAlert(String priceCommand, long chatId) {
        if (null != priceCommand) {
            String[] priceArgs = extractPriceAlertCommand(priceCommand);

            // Destruct the price command
            String priceAlertOperation = priceArgs[0];
            String priceAlertAmount = priceArgs[1];

            if (isWhiteListedPriceAlertCommand(priceAlertOperation)) {

                BigDecimal price = new BigDecimal(priceAlertAmount);

                boolean noDuplicateAlerts = alertRepository.findDuplicateActiveAlerts(chatId, price, priceAlertOperation).isEmpty();
                boolean isMaxActiveAlertList = alertRepository.findAllActiveAlertsByChatId(chatId).size() > 4;

                if(isMaxActiveAlertList) {
                    replyMessage(chatId, "⚠️ Max active alerts are limited to 5");
                    return false;
                }

                if (noDuplicateAlerts) {
                    Alert alert = new Alert();
                    alert.setAlerted(false);
                    alert.setPrice(price);
                    alert.setChatId(chatId);
                    alert.setTriggerCondition(priceAlertOperation.toLowerCase());
                    alertRepository.save(alert);

                    replyMessage(chatId, "✅ New price alert saved");

                    return true;

                } else {
                    replyMessage(chatId, "⚠️ Similar alert already exist");
                }
            } else {
                replyMessage(chatId, "⚠️ Supported price alert operations are only `<` and `>`");
            }
        } else {
            replyMessage(chatId, "⚠️ Please use valid price alert command (> 139500)");
        }
        return false;
    }

    public String formatCurrency(double price) {
        //NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        return currency.format(price);
    }

    public String[] extractUserCommand(String message) {
        int maxArray = 3;
        String[] splitArgs = Arrays.stream(message.split(" ")).limit(maxArray).toArray(String[]::new);

        if (splitArgs.length == 3) {
            return List.of(splitArgs[0], splitArgs[1] + " " + splitArgs[2]).toArray(String[]::new);
        } else {
            return splitArgs;
        }
    }

    public String[] extractPriceAlertCommand(String message) {
        return Arrays.stream(message.split(" ")).limit(2).toArray(String[]::new);
    }

    public boolean isWhiteListedPriceAlertCommand(String priceAlertCommand) {
        String[] whitelistedOperation = {"<", ">"};
        return Arrays.asList(whitelistedOperation).contains(priceAlertCommand);
    }

    public void replyMessage(long chatId, String message) {
        bot.execute(
                new SendMessage(chatId, message)
        );
    }

    public void sendAlert(Crypto crypto, Alert alert) {
        String sb = "\uD83D\uDEA8 New alert:\n" +
                "1 BTC ≈ " +
                formatCurrency(crypto.getPrice().doubleValue()) +
                " " +
                alert.getTriggerCondition() +
                " " +
                formatCurrency(alert.getPrice().doubleValue());

        replyMessage(alert.getChatId(), sb);

        alert.setAlerted(true);
        alertRepository.save(alert);
    }
}
