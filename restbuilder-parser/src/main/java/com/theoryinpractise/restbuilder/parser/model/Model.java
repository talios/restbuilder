package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.Lists;

import java.util.List;

public class Model {
    private String aPackage;
    private String aNamespace;
    private List<Operation> operations = Lists.newArrayList();
    private List<Resource> resources = Lists.newArrayList();

    public Model(String aPackage, String aNamespace, List<Object> children) {
        this.aPackage = aPackage;
        this.aNamespace = aNamespace;
        for (Object child : children) {
            if (child instanceof Operation) {
                operations.add((Operation) child);
            }
            if (child instanceof Resource) {
                resources.add((Resource) child);
            }
        }
    }

    public String getPackage() {
        return aPackage;
    }

    public String getNamespace() {
        return aNamespace;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public List<Resource> getResources() {
        return resources;
    }
}
