package com.theoryinpractise.restbuilder.codegen.api;

import com.sun.codemodel.internal.JCodeModel;
import com.theoryinpractise.restbuilder.parser.model.RestModel;

import java.io.File;

/**
 * Interface for RESTBuilder Codegenerators.
 */
public interface CodeGenerator {

    void generate(JCodeModel jCodeModel, RestModel restModel, File sourceDirectory);

}
