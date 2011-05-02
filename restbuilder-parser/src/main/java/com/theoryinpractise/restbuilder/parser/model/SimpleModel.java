package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

public class SimpleModel extends AbstractModel {

    public SimpleModel(String aNamespace, List<Object> children) {
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

}
