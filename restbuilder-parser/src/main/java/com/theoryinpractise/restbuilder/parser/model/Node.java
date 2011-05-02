package com.theoryinpractise.restbuilder.parser.model;

import org.parboiled.Context;
import org.parboiled.support.Position;

public class Node implements Level {

    private int level;
    private String fileName;
    private int line;
    private int column;

    public Node(Context context, String fileName) {
        this.level = context.getLevel();
        this.fileName = fileName;
        Position position = context.getInputBuffer().getPosition(context.getStartIndex());
        this.line = position.line;
        this.column = position.column;
    }

    public int getLevel() {
        return level;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getErrorMessage(String message) {
        return String.format("%s:%s:%S %s", getFileName(), getLine(), getColumn(), message);
    }

}
