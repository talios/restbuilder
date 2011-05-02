package com.theoryinpractise.restbuilder.parser.model;

import com.theoryinpractise.restbuilder.parser.BaseClassElement;

import java.util.List;

public interface Operation extends Level, BaseClassElement {
    int getLevel();

    String getName();

    String getComment();

    String getPreamble();

    List<OperationAttribute> getAttributes();
}
