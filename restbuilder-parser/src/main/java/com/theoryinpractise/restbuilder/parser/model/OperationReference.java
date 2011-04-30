package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

public class OperationReference implements Operation {
    private int level;
    private ElementType elementType;
    private OperationDefinition restOperationDefinition;

    public OperationReference(int level,  ElementType elementType, OperationDefinition restOperationDefinition) {
        this.level = level;
        this.elementType = elementType;
        this.restOperationDefinition = restOperationDefinition;
    }

    public int getLevel() {
        return level;
    }

    public ElementType getElementType() {
        return elementType;
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
    public List<? extends Field> getFields() {
        return restOperationDefinition.getFields();
    }

    @Override
    public List<OperationAttribute> getAttributes() {
        return restOperationDefinition.getAttributes();
    }
}
