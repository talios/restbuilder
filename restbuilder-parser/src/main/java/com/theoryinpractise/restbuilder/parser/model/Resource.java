package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.theoryinpractise.restbuilder.parser.MediaTypeElement;

import java.util.List;
import java.util.Map;

public class Resource implements Level, MediaTypeElement {
    public static final Ordering<Field> FIELD_ORDERING = Ordering.from(new Field.FieldCountComparator());
    private int level;
    private ElementType elementType;
    private String preamble;
    private String comment;
    private String resourceName;
    private List<Identifier> identifiers = Lists.newArrayList();
    private List<ResourceAttribute> attributes = Lists.newArrayList();
    private Map<String,Operation> operations = Maps.newHashMap();
    private Map<String,View> views = Maps.newHashMap();

    public Resource(int level, String comment, String resourceName, List children) {
        this.level = level;
        this.elementType = ElementType.MODEL;

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
            if (child instanceof Identifier) {
                identifiers.add((Identifier) child);
            }
            if (child instanceof ResourceAttribute) {
                attributes.add((ResourceAttribute) child);
            }
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

    public int getLevel() {
        return level;
    }

    public ElementType getElementType() {
        return elementType;
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
        return FIELD_ORDERING.sortedCopy(identifiers);
    }

    public List<ResourceAttribute> getAttributes() {
        return FIELD_ORDERING.sortedCopy(attributes);
    }

    public List<Field> getFields() {
        return FIELD_ORDERING.sortedCopy(
                new ImmutableList.Builder<Field>()
                        .addAll(identifiers)
                        .addAll(attributes)
                        .build());
    }

    public Map<String, Operation> getOperations() {
        return operations;
    }

    public Map<String, View> getViews() {
        return views;
    }
}
