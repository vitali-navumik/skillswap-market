package com.vitali.framework.invocations;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.io.IOException;
import java.util.stream.Stream;

public interface GenericAPITestTemplateInvocationContextProvider<T extends Skippable & Descriptive>
        extends TestTemplateInvocationContextProvider {

    @Override
    default boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return true;
    }

    @Override
    default Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        GenericAPIInvocationContextProvider<T> provider = new GenericAPIInvocationContextProvider<>();
        try {
            return getTestCasesStream().map(provider::getInvocationContext);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Stream<T> getTestCasesStream() throws IOException;
}
