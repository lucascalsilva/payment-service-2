package com.camunda.training.paymentservice.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Value("${camunda.rest.baseUrl}")
    private String camundaRestUrl;

    @Bean
    public ExternalTaskClient externalTaskClient(){
        return ExternalTaskClient.create()
                .baseUrl(camundaRestUrl)
                .build();
    }
}
