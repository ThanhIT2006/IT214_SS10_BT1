package com.example.bt1.client;

import com.example.bt1.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class ProductServiceClientRT {

    private final RestTemplate restTemplate;

    @Autowired
    public ProductServiceClientRT(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch product info by id using service-id (for Eureka/load-balancer). 
     * Returns a fallback ProductInfo when a timeout occurs.
     */
    public ProductInfo getById(Long productId) {
        String url = "http://product-service/api/products/{id}";
        try {
            return restTemplate.getForObject(url, ProductInfo.class, productId);
        } catch (ResourceAccessException ex) {
            // timeout or IO error -> return fallback
            return fallbackFor(productId);
        } catch (HttpClientErrorException.NotFound nf) {
            // 404 -> product not found
            throw new ProductNotFoundException(productId);
        } catch (HttpClientErrorException httpEx) {
            // other 4xx -> rethrow
            throw httpEx;
        } catch (RestClientException rex) {
            // other client errors -> fallback
            return fallbackFor(productId);
        }
    }

    private ProductInfo fallbackFor(Long productId) {
        return new ProductInfo(productId, "UNKNOWN", BigDecimal.ZERO);
    }
}
