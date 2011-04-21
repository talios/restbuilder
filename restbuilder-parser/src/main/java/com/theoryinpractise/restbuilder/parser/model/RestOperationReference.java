package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

public class RestOperationReference implements RestOperation {
    private int level;
    private RestOperationDefinition restOperationDefinition;

    public RestOperationReference(int level, RestOperationDefinition restOperationDefinition) {
        this.level = level;
        this.restOperationDefinition = restOperationDefinition;
    }

    public int getLevel() {
        return level;
    }

    public RestOperationDefinition getRestOperationDefinition() {
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
    public List<RestAttribute> getAttributes() {
        return restOperationDefinition.getAttributes();
    }
}
