package com.payment.processing.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.ObjectMapper;

@Configuration
public class Beans {
    
    @Bean
    ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }
}
