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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

            // Get the latest BTC price to compare with
            Crypto crypto = cryptoService.getLatestCryptoPrice(
                    "BTC",
                    lunoEndpointUrl
            );

            // Begin price compare here & alert
            activeAlerts.forEach(alert -> {
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
