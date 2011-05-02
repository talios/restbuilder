package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A MultiModel is a meta object which contains a combination view of multiple models, making them appear as a single
 * model.
 */
public class MultiModel extends  AbstractModel {

    List<Model> models = Lists.newArrayList();

    public MultiModel(List<Model> models) {
        this.models = models;
        aPackage = this.models.iterator().next().getPackage();
        aNamespace = this.models.iterator().next().getNamespace();
        operations = mergeOperations(models).build();
        resources = mergeResources(models).build();
    }

    private ImmutableMap.Builder<String, Resource> mergeResources(List<Model> models) {
        ImmutableMap.Builder<String, Resource> builder = ImmutableMap.builder();
        for (Model model : models) {
            builder.putAll(model.getResources());
        }
        return builder;
    }

    private ImmutableMap.Builder<String, Operation> mergeOperations(List<Model> models) {
        ImmutableMap.Builder<String, Operation> builder = ImmutableMap.builder();
        for (Model model : models) {
            builder.putAll(model.getOperations());
        }
        return builder;
    }

}
