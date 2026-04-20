package com.vitali.framework.resolvers;

import com.vitali.framework.enums.UserPreset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface GlobalActionsPreset {
    UserPreset value();
}
