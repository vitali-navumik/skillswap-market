package com.vitali.framework.invocations;

public interface Skippable {
    default boolean skipped() {
        return false;
    }

    default TestSkipParameters testSkipParameters() {
        return new TestSkipParameters();
    }
}
