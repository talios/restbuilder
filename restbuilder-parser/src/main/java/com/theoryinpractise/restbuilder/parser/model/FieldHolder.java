package com.theoryinpractise.restbuilder.parser.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 24/04/11
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FieldHolder {
    List<? extends Field> getFields();
}
