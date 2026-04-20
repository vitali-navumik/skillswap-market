package com.vitali.framework.invocations;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public abstract class TestCaseBase implements Descriptive, Skippable {
    private TestSkipParameters testSkipParameters;
    private String description;
}
