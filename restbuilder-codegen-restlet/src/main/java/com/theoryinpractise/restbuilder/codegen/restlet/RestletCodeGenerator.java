package com.theoryinpractise.restbuilder.codegen.restlet;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;


/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 9/04/11
 * Time: 10:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RestletCodeGenerator extends AbstractGenerator implements CodeGenerator {


    public void generate(JCodeModel jCodeModel, Model model) throws JClassAlreadyExistsException {

        JPackage aPackage = jCodeModel._package(model.getPackage());
        generateModelClasses(jCodeModel, aPackage, model);
        generateResourceClasses(jCodeModel, aPackage, model);

    }

    private void generateResourceClasses(JCodeModel jCodeModel, JPackage p, Model model) throws JClassAlreadyExistsException {

        ResourceClassGenerator resourceClassGenerator = new ResourceClassGenerator(jCodeModel, model);

        for (Resource resource : model.getResources().values()) {

            JDefinedClass valueClass = generateValueClass(jCodeModel, p, resource);
            JDefinedClass identifierClass = generateIdentifierClass(jCodeModel, p, resource);
            generateOperationClasses(jCodeModel, p, resource);

            resourceClassGenerator.generateResourceClass(p, model, resource, valueClass, identifierClass);

        }

    }

    private JDefinedClass generateValueClass(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        String name = camel(resource.getName());
        return generateImmutableBean(jCodeModel, p, name, resource.getFields());
    }

    private void generateOperationClasses(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        for (Operation operation : resource.getOperations().values()) {
            generateImmutableBean(jCodeModel, p.subPackage("operation"), makeOperationClassName(operation), operation.getAttributes());
        }
    }

    private JDefinedClass generateIdentifierClass(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        String identifierName = camel(resource.getName() + "Identifier");
        return generateImmutableBean(jCodeModel, p, identifierName, resource.getIdentifiers());
    }



    private void generateModelClasses(JCodeModel jCodeModel, JPackage p, Model model) throws JClassAlreadyExistsException {
        for (Operation operation : model.getOperations().values()) {
            generateImmutableBean(jCodeModel, p.subPackage("operation"), makeOperationClassName(operation), operation.getAttributes());
        }
    }




}
