package com.theoryinpractise.restbuilder.parser.model;

import org.parboiled.Context;

import java.util.List;

public class OperationReference extends Node implements Operation {
    private Operation restOperationDefinition;
    private String name;

    public OperationReference(Context context,  String filename, String name) {
        super(context, filename);
        this.name = name;
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
