package com.theoryinpractise.restbuilder.parser.model;

import java.util.Map;

public interface Model {

    String getPackage();

    String getNamespace();

    Map<String, Operation> getOperations();

    Map<String, Resource> getResources();

    Model resolve();

}
