package com.vitali.framework.invocations;

import java.util.Collections;
import java.util.List;

public record TestSkipParameters(String reason, List<String> links, List<String> envs) {

    public TestSkipParameters() {
        this("", Collections.emptyList(), Collections.emptyList());
    }
}
