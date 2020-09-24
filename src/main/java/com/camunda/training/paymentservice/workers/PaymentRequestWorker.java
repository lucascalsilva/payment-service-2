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
public class PaymentRequestWorker implements CommandLineRunner {

    private final ExternalTaskClient externalTaskClient;
    private final MessageApi messageApi;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PaymentRequestWorker(ExternalTaskClient externalTaskClient, MessageApi messageApi) {
        this.externalTaskClient = externalTaskClient;
        this.messageApi = messageApi;
    }

    @Override
    public void run(String... args) throws Exception {
        externalTaskClient.subscribe("payment-sending")
                .lockDuration(10000).handler(this::handleTask).open();
    }

    private void handleTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        logger.info("Completing task {} in topic {} ", externalTask.getId(), externalTask.getTopicName());

        Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();
        externalTask.getAllVariables().forEach((s, o) -> {
            variables.put(s.toString(), new VariableValueDto().value(o));
        });

        variables.put("orderId", new VariableValueDto().value(externalTask.getBusinessKey()).type("String"));

        CorrelationMessageDto correlationMessageDto = new CorrelationMessageDto().messageName("PaymentRequestedMessage").resultEnabled(true)
                .businessKey(UUID.randomUUID().toString())
                .processVariables(variables);

        messageApi.deliverMessage(correlationMessageDto);
        externalTaskService.complete(externalTask);
    }
}
