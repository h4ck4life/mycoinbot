package com.filavents.mycoinbot.dto;

public class LunoDto {

    private String btc_price;

    public String getBtc_price() {
        return btc_price;
    }

    @Override
    public String toString() {
        return "LunoDto{" +
                "btc_price='" + btc_price + '\'' +
                '}';
    }
}
