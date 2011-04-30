package com.theoryinpractise.restbuilder.parser.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.theoryinpractise.restbuilder.parser.MediaTypeElement;

import java.util.List;

public class View implements Level, MediaTypeElement {

    public static final Ordering<Field> FIELD_ORDERING = Ordering.from(new Field.FieldCountComparator());

    private int level;
    private ElementType elementType;
    private String preamble;
    private String comment;
    private String resourceName;
    private String viewName;
    private List<ViewAttribute> attributes = Lists.newArrayList();

    public View(int level, String comment, String resourceName, String viewName, List children) {
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
        this.viewName = viewName;
        for (Object child : children) {
            if (child instanceof ViewAttribute) {
                attributes.add((ViewAttribute) child);
            }
        }
    }


    public int getLevel() {
        return level;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getName() {
        return viewName;
    }

    @Override
    public String getMediaTypeName() {
        return getName() + getResourceName().substring(0, 1).toUpperCase() + getResourceName().substring(1);
    }


    public String getPreamble() {
        return preamble;
    }

    public String getComment() {
        return comment;
    }


    public List<ViewAttribute> getAttributes() {
        return FIELD_ORDERING.sortedCopy(attributes);
    }


}
