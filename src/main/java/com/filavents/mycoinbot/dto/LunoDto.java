package com.filavents.mycoinbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LunoDto {

    private String btc_price;

    public String getBtc_price() {
        return btc_price.split(" ")[1].replace(",", "");
    }

    @Override
    public String toString() {
        return "LunoDto{" +
                "btc_price='" + btc_price + '\'' +
                '}';
    }
}
