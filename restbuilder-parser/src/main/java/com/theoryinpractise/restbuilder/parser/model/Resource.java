package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.List;

public class Resource implements Level {
    public static final Ordering<Field> FIELD_ORDERING = Ordering.from(new Field.FieldCountComparator());
    private int level;
    private String comment;
    private String resourceName;
    private List<Identifier> identifiers = Lists.newArrayList();
    private List<Attribute> attributes = Lists.newArrayList();
    private List<Operation> operations = Lists.newArrayList();

    public Resource(int level, String comment, String resourceName, List children) {
        this.level = level;
        this.comment = comment;
        this.resourceName = resourceName;
        for (Object child : children) {
            if (child instanceof Identifier) {
                identifiers.add((Identifier) child);
            }
            if (child instanceof Attribute) {
                attributes.add((Attribute) child);
            }
            if (child instanceof Operation) {
                operations.add((Operation) child);
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

    public List<Identifier> getIdentifiers() {
        return FIELD_ORDERING.sortedCopy(identifiers);
    }

    public List<Attribute> getAttributes() {
        return FIELD_ORDERING.sortedCopy(attributes);
    }

    public List<Field> getFields() {
        return FIELD_ORDERING.sortedCopy(
                new ImmutableList.Builder<Field>()
                        .addAll(identifiers)
                        .addAll(attributes)
                        .build());
    }

    public List<Operation> getOperations() {
        return operations;
    }


}
