package com.filavents.mycoinbot.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TelegramBotTest {

    TelegramBot bot = new TelegramBot();

    @ParameterizedTest
    @CsvSource(value = {
            "139000:RM139,000.00",
            "129301:RM129,301.00",
            "59400:RM59,400.00"
    }, delimiter = ':')
    void formatCurrency_shouldReturn_MYR(BigDecimal input, String expected) {
        assertEquals(expected, bot.formatCurrency(input.doubleValue()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "/alert > 139000:2",
            "/alert :1",
            "/alert >:2",
            "/alert:1"
    }, delimiter = ':')
    void extractUserCommand_shouldReturn_stringArrays(String input, int expected) {
        assertEquals(expected, bot.extractUserCommand(input).length);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "> 139000:2",
            "> 139000 dummy text:2",
            "> :1",
            ">:1"
    }, delimiter = ':')
    void extractPriceAlertCommand(String input, int expected) {
        assertEquals(expected, bot.extractUserCommand(input).length);
    }

    @ParameterizedTest
    @CsvSource(value = {
            ">:true",
            "<:true",
            "=:false",
            "*:false"
    }, delimiter = ':')
    void isWhiteListedPriceAlertCommand_shouldReturn_trueIfMatch_orFalseIfNot(String input, boolean expected) {
        assertEquals(expected, bot.isWhiteListedPriceAlertCommand(input));
    }
}