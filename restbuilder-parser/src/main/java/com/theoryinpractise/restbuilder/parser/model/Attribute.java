package com.theoryinpractise.restbuilder.parser.model;

public class Attribute implements Level {

    private int level;
    private String comment;
    private String attributeType;
    private String attributeName;

    public Attribute(int level, String comment, String attributeType, String attributeName) {
        this.level = level;
        this.comment = comment;
        this.attributeType = attributeType;
        this.attributeName = attributeName;
    }

    public int getLevel() {
        return level;
    }

    public String getComment() {
        return comment;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
