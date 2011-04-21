package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

public class RestOperationDefinition implements RestOperation {
    private int level;
    private String comment;
    private String operationName;
    private List<RestAttribute> attributes;

    public RestOperationDefinition(int level, String comment, String operationName, List<RestAttribute> attributes) {
        this.level = level;
        this.comment = comment;
        this.operationName = operationName;
        this.attributes = attributes;
    }

    public int getLevel() {
        return level;
    }

    public String getComment() {
        return comment;
    }

    public String getName() {
        return operationName;
    }

    public List<RestAttribute> getAttributes() {
        return attributes;
    }
}
