package com.camunda.training.paymentservice;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

public class ProcessUnitTest {

    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Before
    public void setup(){
        init(rule.getProcessEngine());
    }

    @Deployment(resources = "payment-process.bpmn")
    @Test
    public void testHappyPath(){
        ProcessInstance processInstance = runtimeService().startProcessInstanceByMessage("PaymentRequestedMessage");

        assertThat(processInstance).isStarted().externalTask("ChargeCreditTask").hasTopicName("credit-charging");
        complete(externalTask(), withVariables("creditSufficient", false));

        assertThat(processInstance).externalTask("ChargeCreditCardTask").hasTopicName("credit-card-charging");
        complete(externalTask());

        assertThat(processInstance).externalTask("PaymentDoneEvent").hasTopicName("payment-finishing");
        complete(externalTask());

        assertThat(processInstance).isEnded();
    }

}
