package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.Lists;

import java.util.List;

public class RestResource implements Level {
    private int level;
    private String comment;
    private String resourceName;
    private List<RestAttribute> attributes = Lists.newArrayList();
    private List<RestOperation> operations = Lists.newArrayList();

    public RestResource(int level, String comment,  String resourceName, List children) {
        this.level = level;
        this.comment = comment;
        this.resourceName = resourceName;
        for (Object child : children) {
            if (child instanceof RestAttribute) {
                attributes.add((RestAttribute) child);
            }
            if (child instanceof RestOperation) {
                operations.add((RestOperation) child);
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

    public List<RestAttribute> getAttributes() {
        return attributes;
    }

    public List<RestOperation> getOperations() {
        return operations;
    }
}
