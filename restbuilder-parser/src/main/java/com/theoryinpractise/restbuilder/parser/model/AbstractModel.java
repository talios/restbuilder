package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.Maps;

import java.util.Map;

public abstract class AbstractModel implements Model  {

    protected String aNamespace;
    protected Map<String, Operation> operations = Maps.newHashMap();
    protected Map<String, Resource> resources = Maps.newHashMap();

    @Override
    public String getNamespace() {
        return aNamespace;
    }

    @Override
    public Map<String, Operation> getOperations() {
        return operations;
    }

    @Override
    public Map<String, Resource> getResources() {
        return resources;
    }

    @Override
    public Model resolve() {

        // resolve operation references
        for (Resource resource : getResources().values()) {
            for (Operation operation : resource.getOperations().values()) {
                if (operation instanceof OperationReference) {
                    OperationReference reference = (OperationReference) operation;

                    Operation operationDefinition = getOperations().get(reference.getName());
                    if (operationDefinition != null) {
                        reference.setRestOperationDefinition(operationDefinition);
                    } else {
                        throw new RuntimeException(reference.getErrorMessage("Undefined operation: " + reference.getName()));
                    }

                }
            }
        }

        return this;

    }

}
