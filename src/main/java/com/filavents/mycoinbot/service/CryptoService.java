package com.filavents.mycoinbot.service;

import com.filavents.mycoinbot.model.Crypto;

public interface CryptoService {

    Crypto getLatestCryptoPrice(String currencyCode, String lunoUrl) throws Exception;
}
