package com.theoryinpractise.restbuilder.parser;

import com.theoryinpractise.restbuilder.parser.model.Field;

import java.util.List;

public interface BaseClassElement {

    String getPreamble();

    String getComment();

    String getMediaTypeName();

    List<? extends Field> getFields();

}
