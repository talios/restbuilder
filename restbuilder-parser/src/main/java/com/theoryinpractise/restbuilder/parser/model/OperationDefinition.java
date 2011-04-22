package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

public class OperationDefinition implements Operation {
    private int level;
    private String comment;
    private String operationName;
    private List<Attribute> attributes;

    public OperationDefinition(int level, String comment, String operationName, List<Attribute> attributes) {
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

    public List<Attribute> getAttributes() {
        return attributes;
    }
}
