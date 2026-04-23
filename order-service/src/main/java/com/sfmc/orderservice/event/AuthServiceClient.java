package com.sfmc.orderservice.event;

import com.sfmc.orderservice.dto.ClientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public ClientDTO getClientById(Long clientId) {
    	System.out.println("id du client"+clientId);
        try {
        	System.out.println("id dans le try"+clientId);

        	String url = authServiceUrl + "/users/internal/" + clientId;
        	System.out.println("URL APPELEE = " + url);
        	return restTemplate.getForObject(url, ClientDTO.class);
        } catch (Exception e) {
            log.warn("Auth Service indisponible pour client ID {} : {}", clientId, e.getMessage());
            return null;
        }
    }

	
}