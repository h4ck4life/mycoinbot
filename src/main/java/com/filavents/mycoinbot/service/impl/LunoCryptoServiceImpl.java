package com.filavents.mycoinbot.service.impl;

import com.filavents.mycoinbot.dto.LunoDto;
import com.filavents.mycoinbot.model.Crypto;
import com.filavents.mycoinbot.model.repository.CryptoRepository;
import com.filavents.mycoinbot.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class LunoCryptoServiceImpl implements CryptoService {

    @Autowired
    private CryptoRepository cryptoRepository;

    private final WebClient webClient = WebClient.create();

    @Override
    public Crypto getLatestCryptoPrice(String currencyCode) {

        Optional<LunoDto> lunoDto = Optional.ofNullable(
                this.webClient.get()
                        .uri("https://ajax.luno.com/ajax/1/display_ticker")
                        .retrieve()
                        .bodyToMono(LunoDto.class)
                        .block()
        );

        Crypto crypto = new Crypto();
        crypto.setName("BTC");
        lunoDto.ifPresentOrElse(
                lunoDto1 -> crypto.setPrice(new BigDecimal(lunoDto1.getBtc_price())),
                () -> crypto.setPrice(BigDecimal.ZERO)
        );

        cryptoRepository.save(crypto);

        return crypto;

    }

}
