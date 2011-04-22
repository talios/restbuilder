package com.theoryinpractise.restbuilder.parser.model;

import java.util.Comparator;

public abstract class Field implements Level {

    private static int fieldCount = 0;

    private int level;
    private Integer order;
    private String comment;
    private String attributeType;
    private String attributeName;

    public Field(int level, String comment, String attributeType, String attributeName) {
        this.level = level;
        this.order = Integer.valueOf(fieldCount++);
        this.comment = comment;
        this.attributeType = attributeType;
        this.attributeName = attributeName;
    }

    public int getLevel() {
        return level;
    }

    public Integer getOrder() {
        return order;
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

    public static class FieldCountComparator implements Comparator<Field> {
        @Override
        public int compare(Field field, Field field1) {
            return field.getOrder().compareTo(field1.getOrder());
        }
    }

}
