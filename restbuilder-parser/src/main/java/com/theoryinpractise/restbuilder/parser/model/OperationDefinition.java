package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

public class OperationDefinition implements Operation {
    private int level;
    private String preamble;
    private String comment;
    private String operationName;
    private List<OperationAttribute> attributes;

    public OperationDefinition(int level, String comment, String operationName, List<OperationAttribute> attributes) {
        this.level = level;
        this.operationName = operationName;
        this.attributes = attributes;

        if (comment != null) {
            int index = comment.indexOf("\n\n");
            if (index != -1) {
                this.preamble = comment.substring(0, index).trim();
                this.comment = comment.substring(index).trim();
            } else {
                this.preamble = comment;
                this.comment = "";
            }
        } else {
            this.comment = "";
            this.preamble = "";
        }

    }

    public int getLevel() {
        return level;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String getPreamble() {
        return preamble;
    }

    public String getName() {
        return operationName;
    }

    @Override
    public String getMediaTypeName() {
        return getName();
    }

    @Override
    public List<? extends Field> getFields() {
        return attributes;
    }

    public List<OperationAttribute> getAttributes() {
        return attributes;
    }
}
