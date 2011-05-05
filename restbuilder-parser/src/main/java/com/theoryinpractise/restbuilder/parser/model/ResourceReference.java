package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.theoryinpractise.restbuilder.parser.BaseClassElement;
import org.parboiled.Context;

import java.util.List;
import java.util.Map;

public class ResourceReference extends Node implements BaseClassElement {

    public static final Ordering<Field> FIELD_ORDERING = Ordering.from(new Field.FieldCountComparator());
    private boolean unary;
    private String preamble;
    private String comment;
    private String resourceName;
    private Map<String,Operation> operations = Maps.newHashMap();
    private Map<String,View> views = Maps.newHashMap();
    private Resource resource;

    public ResourceReference(Context context, String filename, boolean unary, String comment, String resourceName, List children) {
        super(context, filename);

        this.unary = unary;

        if (comment != null) {
            int index = comment.indexOf("\n\n");
            if (index != -1) {
                this.preamble = comment.substring(0, index).trim();
                this.comment = comment.substring(index).trim();
            } else {
                this.preamble = comment;
                this.comment = "";
            }
        } else {
            this.comment = "";
            this.preamble = "";
        }

        this.resourceName = resourceName;
        for (Object child : children) {
            if (child instanceof Operation) {
                Operation operation = (Operation) child;
                operations.put(operation.getName(), operation);
            }
            if (child instanceof View) {
                View view = (View) child;
                views.put(view.getName(), view);
            }
        }
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getName() {
        return resourceName;
    }

    @Override
    public String getMediaTypeName() {
        return getName();
    }

    public String getPreamble() {
        return preamble;
    }

    public String getComment() {
        return comment;
    }

    public List<Identifier> getIdentifiers() {
        return FIELD_ORDERING.sortedCopy(resource.getIdentifiers());
    }

    public List<ResourceAttribute> getAttributes() {
        return FIELD_ORDERING.sortedCopy(resource.getAttributes());
    }

    public List<Field> getFields() {
        return FIELD_ORDERING.sortedCopy(
                new ImmutableList.Builder<Field>()
                        .addAll(getIdentifiers())
                        .addAll(getAttributes())
                        .build());
    }

    public Map<String, Operation> getOperations() {
        return operations;
    }

    public Map<String, View> getViews() {
        return views;
    }
}
