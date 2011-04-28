package com.theoryinpractise.restbuilder.codegen.base;

import com.google.common.collect.Maps;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 26/04/11
 * Time: 10:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelGenerator extends AbstractGenerator {

    public JDefinedClass generateValueClass(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        String name = camel(resource.getName());
        return generateImmutableBean(jCodeModel, p, name, resource.getFields());
    }

    public void generateOperationClasses(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        for (Operation operation : resource.getOperations().values()) {
            generateImmutableBean(jCodeModel, p.subPackage("operation"), makeOperationClassName(operation), operation.getAttributes());
        }
    }

    public JDefinedClass generateIdentifierClass(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        String identifierName = camel(resource.getName() + "Identifier");
        return generateImmutableBean(jCodeModel, p, identifierName, resource.getIdentifiers());
    }


    public void generateModelClasses(JCodeModel jCodeModel, JPackage p, Model model) throws JClassAlreadyExistsException {
        for (Operation operation : model.getOperations().values()) {
            generateImmutableBean(jCodeModel, p.subPackage("operation"), makeOperationClassName(operation), operation.getAttributes());
        }
    }

    public CodeGenModelMirror mirrorOf(JCodeModel codeModel, Model model) throws JClassAlreadyExistsException {
        return new CodeGenModelMirror(codeModel, model);
    }


    public class CodeGenModelMirror {

        private final JCodeModel codeModel;

        private final Model model;

        private final JPackage modelPackage;

        private Map<String, JDefinedClass> operationClasses = Maps.newHashMap();

        private Map<String, JDefinedClass> resourceClasses = Maps.newHashMap();

        private Map<String, JDefinedClass> resourceIdentifierClasses = Maps.newHashMap();

        public CodeGenModelMirror(JCodeModel codeModel, Model model) throws JClassAlreadyExistsException {
            this.codeModel = codeModel;
            this.model = model;

            this.modelPackage = codeModel._package(model.getPackage());

            for (Operation operation : model.getOperations().values()) {
                generateOperationClass(codeModel, operation);
            }

            for (Resource resource : model.getResources().values()) {

                resourceClasses.put(resource.getName(), generateValueClass(codeModel, modelPackage, resource));

                resourceIdentifierClasses.put(resource.getName(), generateIdentifierClass(codeModel, modelPackage, resource));

                for (Operation operation : resource.getOperations().values()) {
                    generateOperationClass(codeModel, operation);
                }

            }

        }

        private void generateOperationClass(JCodeModel codeModel, Operation operation) throws JClassAlreadyExistsException {
            try {
                operationClasses.put(
                        operation.getName(),
                        generateImmutableBean(codeModel, modelPackage.subPackage("operation"),
                                makeOperationClassName(operation), operation.getAttributes()));
            } catch (JClassAlreadyExistsException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        public Model getModel() {
            return model;
        }

        public JPackage getPackage() {
            return modelPackage;
        }

        public Map<String, JDefinedClass> getOperations() {
            return operationClasses;
        }

        public Map<String, JDefinedClass> getResources() {
            return resourceClasses;
        }

        public Map<String, JDefinedClass> getResourceIdentifiers() {
            return resourceIdentifierClasses;
        }
    }


}
