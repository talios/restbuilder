package com.theoryinpractise.restbuilder.parser.model;

import com.theoryinpractise.restbuilder.parser.MediaTypeElement;

import java.util.List;

public interface Operation extends Level, FieldHolder, MediaTypeElement {
    int getLevel();

    String getName();

    String getComment();

    String getPreamble();

    List<OperationAttribute> getAttributes();
}
