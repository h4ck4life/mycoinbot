package com.filavents.mycoinbot.controller;

import com.filavents.mycoinbot.model.Alert;
import com.filavents.mycoinbot.model.Crypto;
import com.filavents.mycoinbot.model.repository.AlertRepository;
import com.filavents.mycoinbot.service.CryptoService;
import com.filavents.mycoinbot.service.impl.LunoCryptoServiceImpl;
import com.filavents.mycoinbot.startup.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("crypto")
public class CryptoController {

    Logger logger = LoggerFactory.getLogger(CryptoController.class);

    @Value("${mycoinbot.lunoEndpointUrl}")
    private String lunoEndpointUrl;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private final CryptoService cryptoService = new LunoCryptoServiceImpl();

    @GetMapping(path = "/latest")
    public ResponseEntity<Crypto> getLatestCryptoPrice() throws Exception {
        Crypto crypto = cryptoService.getLatestCryptoPrice(
                "BTC",
                lunoEndpointUrl
        );
        return new ResponseEntity<>(crypto, HttpStatus.OK);
    }

    @GetMapping(path = "/alert")
    public ResponseEntity<String> checkPriceAlert() throws Exception {

        List<Alert> activeAlerts = alertRepository.findAllActiveAlerts();

        if(!activeAlerts.isEmpty()) {
            Crypto currentBTCPrice = TelegramBot.cache.getIfPresent((TelegramBot.CACHE_KEY_BTCPRICE));
            if(null == currentBTCPrice) {
                logger.info("Get live price..");
                currentBTCPrice = cryptoService.getLatestCryptoPrice(
                        "BTC",
                        lunoEndpointUrl
                );
                TelegramBot.cache.put(TelegramBot.CACHE_KEY_BTCPRICE, currentBTCPrice);
            } else {
                logger.info("Get price from cache..");
            }

            // Begin price compare here & alert
            Crypto finalCurrentBTCPrice = currentBTCPrice;
            logger.info("BTC price is: " + telegramBot.formatCurrency(finalCurrentBTCPrice.getPrice().doubleValue()));
            activeAlerts.forEach(alert -> {
                switch (alert.getTriggerCondition()) {
                    case ">":
                        if(finalCurrentBTCPrice.getPrice().doubleValue() > alert.getPrice().doubleValue()) {
                            telegramBot.sendAlert(finalCurrentBTCPrice, alert);
                        }
                        break;
                    case "<":
                        if(finalCurrentBTCPrice.getPrice().doubleValue() < alert.getPrice().doubleValue()) {
                            telegramBot.sendAlert(finalCurrentBTCPrice, alert);
                        }
                        break;
                    default:
                }
            });
        }

        return new ResponseEntity<>("Total active alerts: " + activeAlerts.size(), HttpStatus.OK);
    }

}
