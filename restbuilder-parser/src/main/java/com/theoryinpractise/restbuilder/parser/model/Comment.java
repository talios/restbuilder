package com.theoryinpractise.restbuilder.parser.model;

public abstract class Comment implements Level {

    public Comment(int level, String comment) {
        this.level = level;
        this.comment = comment;
    }

    private int level;

    public int getLevel() {
        return level;
    }

    private String comment;

    public String getComment() {
        return comment;
    }

    public String toString() {
        return comment;
    }


    public static class OperationComment extends Comment {
        public OperationComment(int level, String comment) {
            super(level, comment);
        }
    }

    public static class FieldComment extends Comment {
        public FieldComment(int level, String comment) {
            super(level, comment);
        }
    }

    public static class ResourceComment extends Comment {
        public ResourceComment(int level, String comment) {
            super(level, comment);
        }
    }

    public static class ResourceReferenceComment extends Comment {
        public ResourceReferenceComment(int level, String comment) {
            super(level, comment);
        }
    }

    public static class ViewComment extends Comment {
        public ViewComment(int level, String comment) {
            super(level, comment);
        }
    }

}
