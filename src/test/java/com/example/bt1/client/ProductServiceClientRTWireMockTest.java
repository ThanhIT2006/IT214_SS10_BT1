package com.example.bt1.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductServiceClientRTWireMockTest {

    private WireMockServer wire;

    @BeforeEach
    void startWireMock() {
        wire = new WireMockServer(0);
        wire.start();
    }

    @AfterEach
    void stopWireMock() {
        if (wire != null) wire.stop();
    }

    @Test
    void integration_with_wiremock_returnsProduct() {
        Long id = 7L;
        String body = "{\"id\":7,\"name\":\"WM-Product\",\"price\":19.99}";

        wire.stubFor(get(urlEqualTo("/api/products/7"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatus(200)));

        int port = wire.port();
        String baseUrl = "http://localhost:" + port;

        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(3000);
        RestTemplate rt = new RestTemplate(f);

        ProductServiceClientRT client = new ProductServiceClientRT(rt, baseUrl);
        ProductInfo pi = client.getById(id);

        assertEquals(id, pi.getId());
        assertEquals("WM-Product", pi.getName());
        assertEquals(0, pi.getPrice().compareTo(new BigDecimal("19.99")));
    }
}
