package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 9/04/11
 * Time: 10:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Operation extends Level, FieldHolder {
    int getLevel();

    String getName();

    String getComment();

    String getPreamble();

    List<Attribute> getAttributes();
}
