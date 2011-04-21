package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.Lists;

import java.util.List;

public class RestModel {
    private String aPackage;
    private String aNamespace;
    private List<RestOperation> operations = Lists.newArrayList();
    private List<RestResource> resources = Lists.newArrayList();

    public RestModel(String aPackage, String aNamespace, List<Object> children) {
        this.aPackage = aPackage;
        this.aNamespace = aNamespace;
        for (Object child : children) {
            if (child instanceof RestOperation) {
                operations.add((RestOperation) child);
            }
            if (child instanceof RestResource) {
                resources.add((RestResource) child);
            }
        }
    }

    public String getaPackage() {
        return aPackage;
    }

    public String getaNamespace() {
        return aNamespace;
    }

    public List<RestOperation> getOperations() {
        return operations;
    }

    public List<RestResource> getResources() {
        return resources;
    }
}
