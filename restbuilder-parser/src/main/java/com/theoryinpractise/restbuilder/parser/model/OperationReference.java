package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

public class OperationReference implements Operation {
    private int level;
    private OperationDefinition restOperationDefinition;

    public OperationReference(int level, OperationDefinition restOperationDefinition) {
        this.level = level;
        this.restOperationDefinition = restOperationDefinition;
    }

    public int getLevel() {
        return level;
    }

    public OperationDefinition getRestOperationDefinition() {
        return restOperationDefinition;
    }

    @Override
    public String getName() {
        return restOperationDefinition.getName();
    }

    @Override
    public String getComment() {
        return restOperationDefinition.getComment();
    }

    @Override
    public String getPreamble() {
        return restOperationDefinition.getPreamble();
    }

    @Override
    public List<Attribute> getAttributes() {
        return restOperationDefinition.getAttributes();
    }
}
