package com.vitali.framework.resolvers;

import com.vitali.framework.enums.UserPreset;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class GlobalActionsParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == ActionsContainer.class
                && parameterContext.isAnnotated(GlobalActionsPreset.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        UserPreset preset = parameterContext.findAnnotation(GlobalActionsPreset.class)
                .orElseThrow(() -> new ParameterResolutionException("Annotation @GlobalActionsPreset not found"))
                .value();

        return UserCreationHelper.createUserAndLogIn(preset);
    }
}
