package com.vitali.framework.invocations;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SkipTestInInvocationExecutionCondition implements ExecutionCondition {

    private final TestSkipParameters testSkipParameters;

    public SkipTestInInvocationExecutionCondition(Skippable testCase) {
        this.testSkipParameters = testCase.testSkipParameters();
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String environment = System.getProperty("env");

        if (testSkipParameters != null && !testSkipParameters.envs().isEmpty()) {
            for (String env : testSkipParameters.envs()) {
                if (env.equalsIgnoreCase(environment)) {
                    return ConditionEvaluationResult.disabled(String.format(
                            "Disabled on the %s environment due: %s. Links: %s",
                            env,
                            testSkipParameters.reason(),
                            String.join(",", testSkipParameters.links())
                    ));
                }
            }
        }

        return ConditionEvaluationResult.enabled(String.format("Enabled on the %s environment", environment));
    }
}
