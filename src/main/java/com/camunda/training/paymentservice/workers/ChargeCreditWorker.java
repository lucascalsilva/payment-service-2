package com.camunda.training.paymentservice.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChargeCreditWorker implements CommandLineRunner {

    private final ExternalTaskClient externalTaskClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ChargeCreditWorker(ExternalTaskClient externalTaskClient) {
        this.externalTaskClient = externalTaskClient;
    }

    @Override
    public void run(String... args) throws Exception {
        externalTaskClient.subscribe("credit-charging")
                .handler(this::handleTask).open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        logger.info("Completing task {} in topic {} ", externalTask.getId(), externalTask.getTopicName());
        Double amount = (Double) externalTask.getVariable("amount");
        Boolean creditSufficient = true;

        if(amount != null && amount >= 100){
            creditSufficient = false;
        }

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("creditSufficient", creditSufficient);

        externalTaskService.complete(externalTask, variables);
    }
}
