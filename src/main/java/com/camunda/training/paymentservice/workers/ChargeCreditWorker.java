package com.camunda.training.paymentservice.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class ChargeCreditWorker implements CommandLineRunner {

    private final ExternalTaskClient externalTaskClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Double customerCredit = 1000.0;

    public ChargeCreditWorker(ExternalTaskClient externalTaskClient) {
        this.externalTaskClient = externalTaskClient;
    }

    @Override
    public void run(String... args) throws Exception {
        externalTaskClient.subscribe("credit-charging")
                .handler(this::handleTask).lockDuration(10000L).open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        logger.info("Completing task {} in topic {} ", externalTask.getId(), externalTask.getTopicName());
        try {
            Double amount = (Double) externalTask.getVariable("amount");
            Boolean doFail = (Boolean) externalTask.getVariable("doFail");

            if(doFail){
                throw new RuntimeException("Process failed");
            }

            Boolean creditSufficient = true;
            Double remainingAmount = Math.max(amount - customerCredit, 0);

            Map<String, Object> variables = new HashMap<String, Object>();
            if (remainingAmount > 0) {
                creditSufficient = false;
            }

            variables.put("remainingAmount", remainingAmount);
            variables.put("creditSufficient", creditSufficient);

            externalTaskService.complete(externalTask, variables);
        }
        catch(Exception e){
            logger.info("Task {} in topic {} has a failure", externalTask.getId(), externalTask.getTopicName());
            Integer retries = null;
            if(externalTask.getRetries() == null){
                retries = 3;
            }
            else{
                retries = externalTask.getRetries() - 1;
            }
            externalTaskService.handleFailure(externalTask, e.getMessage(), Arrays.toString(e.getStackTrace()), retries, 5000);
        }
    }
}
