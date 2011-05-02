package com.theoryinpractise.restbuilder.codegen.api;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.theoryinpractise.restbuilder.parser.model.Model;

/**
 * Interface for RESTBuilder Codegenerators.
 */
public interface CodeGenerator {

    void generate(JCodeModel jCodeModel, Model model, String packageName) throws JClassAlreadyExistsException;

}
