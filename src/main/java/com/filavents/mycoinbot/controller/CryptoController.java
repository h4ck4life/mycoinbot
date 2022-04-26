package com.filavents.mycoinbot.controller;

import com.filavents.mycoinbot.model.Crypto;
import com.filavents.mycoinbot.service.CryptoService;
import com.filavents.mycoinbot.service.impl.LunoCryptoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
@RequestMapping("crypto")
public class CryptoController {

    @Autowired
    private final CryptoService cryptoService = new LunoCryptoServiceImpl();

    @RequestMapping(value = "/latest", method = RequestMethod.GET)
    public ResponseEntity<Crypto> getLatestCryptoPrice() {
        Crypto latestPrice = cryptoService.getLatestCryptoPrice("BTC");
        System.out.println(latestPrice.toString());
        return null;
    }
}
