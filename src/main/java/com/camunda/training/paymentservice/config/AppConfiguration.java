package com.camunda.training.paymentservice.config;

import com.camunda.consulting.client.invoker.ApiClient;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Value("${camunda.rest.baseUrl}")
    private String camundaRestUrl;

    private final ApiClient apiClient;

    public AppConfiguration(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Bean
    public ExternalTaskClient externalTaskClient(){
        return ExternalTaskClient.create()
                .baseUrl(camundaRestUrl)
                .disableBackoffStrategy()
                .asyncResponseTimeout(60000)
                .build();
    }

    @Bean
    public ApiClient apiClient(){
        apiClient.setBasePath("http://localhost:8080/engine-rest");

        return apiClient;
    }


}
