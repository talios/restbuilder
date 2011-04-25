package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class Model {
    private String aPackage;
    private String aNamespace;
    private Map<String, Operation> operations = Maps.newHashMap();
    private Map<String, Resource> resources = Maps.newHashMap();

    public Model(String aPackage, String aNamespace, List<Object> children) {
        this.aPackage = aPackage;
        this.aNamespace = aNamespace;
        for (Object child : children) {
            if (child instanceof Operation) {
                Operation operation = (Operation) child;
                operations.put(operation.getName(), operation);
            }
            if (child instanceof Resource) {
                Resource resource = (Resource) child;
                resources.put(resource.getName(), resource);
            }
        }
    }

    public String getPackage() {
        return aPackage;
    }

    public String getNamespace() {
        return aNamespace;
    }

    public Map<String, Operation> getOperations() {
        return operations;
    }

    public Map<String, Resource> getResources() {
        return resources;
    }
}
