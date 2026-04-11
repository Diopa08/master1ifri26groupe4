package com.sfmc.orderservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class BillingServiceClient {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${billing.service.url:http://localhost:8082}")
    private String billingServiceUrl;

    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> generateInvoice(BillingRequest request) {
        String url = billingServiceUrl + "/api/invoices/generate";
        log.info("Appel Billing Service POST {}", url);
        return (ResponseEntity<Map<String, Object>>)
                (ResponseEntity<?>) restTemplate.postForEntity(url, request, Map.class);
    }
}