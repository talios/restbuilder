package com.theoryinpractise.restbuilder.parser.model;

public abstract class Comment implements Level {

    public Comment(int level, ElementType elementType, String comment) {
        this.level = level;
        this.elementType = elementType;
        this.comment = comment;
    }

    private int level;

    public int getLevel() {
        return level;
    }

    private ElementType elementType;

    public ElementType getElementType() {
        return elementType;
    }

    private String comment;

    public String getComment() {
        return comment;
    }

    public String toString() {
        return comment;
    }


    public static class OperationComment extends Comment {
        public OperationComment(int level, ElementType elementType, String comment) {
            super(level, elementType, comment);
        }
    }

    public static class FieldComment extends Comment {
        public FieldComment(int level, ElementType elementType, String comment) {
            super(level, elementType, comment);
        }
    }

    public static class ResourceComment extends Comment {
        public ResourceComment(int level, ElementType elementType, String comment) {
            super(level, elementType, comment);
        }
    }

    public static class ViewComment extends Comment {
        public ViewComment(int level, ElementType elementType, String comment) {
            super(level, elementType, comment);
        }
    }

}
