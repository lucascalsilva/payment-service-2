package com.camunda.training.paymentservice.workers;

import com.camunda.consulting.client.api.MessageApi;
import com.camunda.consulting.client.model.CorrelationMessageDto;
import com.camunda.consulting.client.model.VariableValueDto;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentFinishedWorker implements CommandLineRunner {

    private final ExternalTaskClient externalTaskClient;
    private final MessageApi messageApi;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PaymentFinishedWorker(ExternalTaskClient externalTaskClient, MessageApi messageApi) {
        this.externalTaskClient = externalTaskClient;
        this.messageApi = messageApi;
    }

    @Override
    public void run(String... args) throws Exception {
        externalTaskClient.subscribe("payment-finishing").lockDuration(10000)
                .handler(this::handleTask).open();

    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        logger.info("Completing task {} in topic {} ", externalTask.getId(), externalTask.getTopicName());
        String orderId = (String) externalTask.getVariable("orderId");
        Boolean paymentOk = (Boolean) externalTask.getVariable("paymentOk");

        CorrelationMessageDto correlationMessageDto = new CorrelationMessageDto().messageName("PaymentReceivedMessage")
                .resultEnabled(true)
                .putProcessVariablesItem("paymentOk", new VariableValueDto().value(paymentOk).type("Boolean"))
                .businessKey(orderId);

        messageApi.deliverMessage(correlationMessageDto);
        externalTaskService.complete(externalTask);
    }
}
