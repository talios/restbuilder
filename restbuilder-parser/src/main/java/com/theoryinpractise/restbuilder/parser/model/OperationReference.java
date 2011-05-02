package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

public class OperationReference implements Operation {
    private int level;
    private ElementType elementType;
    private Operation restOperationDefinition;
    private String name;

    public OperationReference(int level,  ElementType elementType, String name) {
        this.level = level;
        this.elementType = elementType;
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setRestOperationDefinition(Operation restOperationDefinition) {
        this.restOperationDefinition = restOperationDefinition;
    }

    public Operation getRestOperationDefinition() {
        return restOperationDefinition;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMediaTypeName() {
        return getName();
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
