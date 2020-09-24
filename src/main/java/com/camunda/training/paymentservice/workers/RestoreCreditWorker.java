package com.camunda.training.paymentservice.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RestoreCreditWorker implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExternalTaskClient externalTaskClient;

    public RestoreCreditWorker(ExternalTaskClient externalTaskClient) {
        this.externalTaskClient = externalTaskClient;
    }

    @Override
    public void run(String... args) throws Exception {
        externalTaskClient.subscribe("credit-restoring")
                .lockDuration(10000).handler(this::handleTask).open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        logger.info("Completing task {} in topic {} ", externalTask.getId(), externalTask.getTopicName());
        logger.info("Task {} is a compensation task", externalTask.getId());
        externalTaskService.complete(externalTask);
    }
}
