package com.example.bt1.client;

import com.example.bt1.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientRTTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceClientRT client;

    @Test
    void testGetById_success() {
        Long id = 42L;
        ProductInfo pi = new ProductInfo(id, "TestProduct", new BigDecimal("9.99"));
        when(restTemplate.getForObject(any(String.class), any(Class.class), anyLong())).thenReturn(pi);

        ProductInfo result = client.getById(id);
        assertNotNull(result);
        assertEquals(pi, result);
    }

    @Test
    void testGetById_timeout_returnsFallback() {
        Long id = 100L;
        when(restTemplate.getForObject(any(String.class), any(Class.class), anyLong()))
                .thenThrow(new ResourceAccessException("I/O timeout"));

        ProductInfo result = client.getById(id);
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("UNKNOWN", result.getName());
        assertEquals(0, result.getPrice().compareTo(BigDecimal.ZERO));
    }
}
