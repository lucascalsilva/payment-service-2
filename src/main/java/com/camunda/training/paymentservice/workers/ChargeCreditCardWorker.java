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
public class ChargeCreditCardWorker implements CommandLineRunner {

    private final ExternalTaskClient externalTaskClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ChargeCreditCardWorker(ExternalTaskClient externalTaskClient) {
        this.externalTaskClient = externalTaskClient;
    }

    @Override
    public void run(String... args) throws Exception {
        externalTaskClient.subscribe("credit-card-charging")
                .handler(this::handleTask).lockDuration(10000L).open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Boolean doError = (Boolean) externalTask.getVariable("doError");

        if(doError != null && doError){
            externalTaskService.handleBpmnError(externalTask, "ChargeFailedError", "Charge credit failed.");
        }
        else {
            logger.info("Completing task {} in topic {} ", externalTask.getId(), externalTask.getTopicName());

            Double remainingAmount = externalTask.getVariable("remainingAmount");
            logger.info("The remaining amount is {}", remainingAmount);

            externalTaskService.complete(externalTask);
        }
    }
}
