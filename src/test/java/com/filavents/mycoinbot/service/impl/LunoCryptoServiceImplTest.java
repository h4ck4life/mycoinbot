package com.filavents.mycoinbot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filavents.mycoinbot.model.Crypto;
import com.filavents.mycoinbot.model.repository.CryptoRepository;
import com.filavents.mycoinbot.service.CryptoService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LunoCryptoServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String baseUrl;

    @Mock
    private CryptoRepository cryptoRepository;

    @InjectMocks
    private CryptoService cryptoService = new LunoCryptoServiceImpl();

    public static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
    }

    @Test
    void givenLunoResponse_whenNotEmpty_thenReturnFormattedPrice() throws Exception {

        // Mock the Luno API response
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("btc_price", "BTC/MYR 170,000");
        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader("Content-Type", "application/json"));

        // Mock the repo
        Mockito.when(cryptoRepository.save(Mockito.any())).thenReturn(Mockito.any());

        Crypto crypto = cryptoService.getLatestCryptoPrice("BTC", baseUrl);
        assertEquals(new BigDecimal(170000), crypto.getPrice());
    }

    @Test
    void givenLunoResponse_whenJSONKeyMissing_thenReturnZeroPrice() throws Exception {

        // Mock the Luno API response
        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("wrong_json_key", "BTC/MYR 170,000");
        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader("Content-Type", "application/json"));

        // Mock the repo
        Mockito.when(cryptoRepository.save(Mockito.any())).thenReturn(Mockito.any());

        Crypto crypto = cryptoService.getLatestCryptoPrice("BTC", baseUrl);
        assertEquals(new BigDecimal(0), crypto.getPrice());
    }

    @Test
    void givenLunoEndpoint_whenNotReachable_thenReturnException() throws JsonProcessingException {

        WebClientRequestException exception = assertThrows(WebClientRequestException.class, () -> {
            Crypto crypto = cryptoService.getLatestCryptoPrice("BTC", "http://dummy");
        });
        assertEquals("Failed to resolve 'dummy' after 6 queries ; nested exception is java.net.UnknownHostException: Failed to resolve 'dummy' after 6 queries ", exception.getMessage());
    }
}