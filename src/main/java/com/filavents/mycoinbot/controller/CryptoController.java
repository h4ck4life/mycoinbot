package com.filavents.mycoinbot.controller;

import com.filavents.mycoinbot.model.Alert;
import com.filavents.mycoinbot.model.Crypto;
import com.filavents.mycoinbot.model.repository.AlertRepository;
import com.filavents.mycoinbot.service.CryptoService;
import com.filavents.mycoinbot.service.impl.LunoCryptoServiceImpl;
import com.filavents.mycoinbot.startup.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping("crypto")
public class CryptoController {

    @Value("${mycoinbot.lunoEndpointUrl}")
    private String lunoEndpointUrl;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private final CryptoService cryptoService = new LunoCryptoServiceImpl();

    @RequestMapping(value = "/latest", method = RequestMethod.GET)
    public ResponseEntity<Crypto> getLatestCryptoPrice() throws Exception {
        Crypto crypto = cryptoService.getLatestCryptoPrice(
                "BTC",
                lunoEndpointUrl
        );
        return new ResponseEntity<>(crypto, HttpStatus.OK);
    }

    @RequestMapping(value = "/alert", method = RequestMethod.GET)
    public ResponseEntity<String> checkPriceAlert() throws Exception {

        List<Alert> activeAlerts = alertRepository.findAllActiveAlerts();

        if(activeAlerts.isEmpty() == false) {

            // Get the latest BTC price to compare with
            Crypto crypto = cryptoService.getLatestCryptoPrice(
                    "BTC",
                    lunoEndpointUrl
            );

            // Begin price compare here & alert
            activeAlerts.stream().forEach(alert -> {
                switch (alert.getTriggerCondition()) {
                    case ">":
                        if(crypto.getPrice().doubleValue() > alert.getPrice().doubleValue()) {
                            telegramBot.sendAlert(crypto, alert);
                        }
                        break;
                    case "<":
                        if(crypto.getPrice().doubleValue() < alert.getPrice().doubleValue()) {
                            telegramBot.sendAlert(crypto, alert);
                        }
                        break;
                    default:
                }
            });
        }

        return new ResponseEntity<>("Total active alerts: " + activeAlerts.size(), HttpStatus.OK);
    }

}
