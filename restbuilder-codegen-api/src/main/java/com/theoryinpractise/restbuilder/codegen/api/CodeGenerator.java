package com.theoryinpractise.restbuilder.codegen.api;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.theoryinpractise.restbuilder.parser.model.RestModel;

/**
 * Interface for RESTBuilder Codegenerators.
 */
public interface CodeGenerator {

    void generate(JCodeModel jCodeModel, RestModel restModel) throws JClassAlreadyExistsException;

}
