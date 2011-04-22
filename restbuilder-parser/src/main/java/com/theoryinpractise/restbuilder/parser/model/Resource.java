package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.Lists;

import java.util.List;

public class Resource implements Level {
    private int level;
    private String comment;
    private String resourceName;
    private List<Attribute> attributes = Lists.newArrayList();
    private List<Operation> operations = Lists.newArrayList();

    public Resource(int level, String comment, String resourceName, List children) {
        this.level = level;
        this.comment = comment;
        this.resourceName = resourceName;
        for (Object child : children) {
            if (child instanceof Attribute) {
                attributes.add((Attribute) child);
            }
            if (child instanceof Operation) {
                operations.add((Operation) child);
            }
        }
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return resourceName;
    }

    public String getComment() {
        return comment;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
