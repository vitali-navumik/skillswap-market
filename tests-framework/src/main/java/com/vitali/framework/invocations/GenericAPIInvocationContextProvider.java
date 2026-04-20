package com.vitali.framework.invocations;

import com.vitali.framework.connectors.ConnectorParameterResolver;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericAPIInvocationContextProvider<T extends Skippable & Descriptive> {

    public TestTemplateInvocationContext getInvocationContext(T testCase) {
        return getInvocationContext(testCase, Collections.emptyList());
    }

    public TestTemplateInvocationContext getInvocationContext(T testCase, List<Extension> extensions) {
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return testCase.description();
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                List<Extension> allExtensions = new ArrayList<>();
                allExtensions.add(new SkipTestInInvocationExecutionCondition(testCase));
                allExtensions.add(new GenericTypedParameterResolver<>(testCase));
                allExtensions.add(new ConnectorParameterResolver());
                allExtensions.addAll(extensions);
                return allExtensions;
            }
        };
    }
}
