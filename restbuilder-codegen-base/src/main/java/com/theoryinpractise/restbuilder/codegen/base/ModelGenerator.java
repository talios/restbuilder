package com.theoryinpractise.restbuilder.codegen.base;

import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import com.theoryinpractise.restbuilder.parser.model.*;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 26/04/11
 * Time: 10:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelGenerator extends AbstractGenerator {

    public ResourceMirror generateResourceMirror(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        String name = camel(resource.getName());
        JDefinedClass valueClass = generateImmutableBean(jCodeModel, p, name, resource.getFields());

        JDefinedClass identifierClass = generateImmutableBean(jCodeModel, p, name + "Identifier", resource.getIdentifiers());

        // URI constant
        StringBuilder sb = new StringBuilder("/" + resource.getName());
        if (!resource.getIdentifiers().isEmpty()) {
            for (Identifier identifier : resource.getIdentifiers()) {
                sb.append("/{").append(identifier.getName()).append("}");
            }
        }
        JFieldVar uriField = valueClass.field(JMod.PUBLIC | JMod.FINAL | JMod.STATIC, jCodeModel.ref(String.class),
                "URI", JExpr.lit(sb.toString()));


        // View classes
        Map<String,ViewMirror> viewMirrors = Maps.newHashMap();
        for (Map.Entry<String, View> entry : resource.getViews().entrySet()) {

            JDefinedClass viewClass = generateImmutableBean(jCodeModel, p, camel(entry.getKey()) + name + "View",
                    resource.getIdentifiers(),
                    entry.getValue().getAttributes());

            viewMirrors.put(entry.getKey(), new ViewMirror(entry.getValue(), viewClass));

        }

        return new ResourceMirror(resource, valueClass, identifierClass, uriField, viewMirrors);
    }

    public CodeGenModelMirror mirrorOf(JCodeModel codeModel, Model model, String packageName) throws JClassAlreadyExistsException {
        return new CodeGenModelMirror(codeModel, model, packageName);
    }


    public class CodeGenModelMirror {

        private final Model model;

        private final JPackage modelPackage;

        private Map<String, JDefinedClass> operationClasses = Maps.newHashMap();

        private Map<String, ResourceMirror> resourceClasses = Maps.newHashMap();

        public CodeGenModelMirror(JCodeModel codeModel, Model model, String packageName) throws JClassAlreadyExistsException {
            this.model = model;

            this.modelPackage = codeModel._package(packageName);

            for (Operation operation : model.getOperations().values()) {
                generateOperationClass(codeModel, operation);
            }

            for (Resource resource : model.getResources().values()) {

                ResourceMirror resourceMirror = generateResourceMirror(codeModel, modelPackage, resource);

                resourceClasses.put(resource.getName(), resourceMirror);

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

        public Map<String, ResourceMirror> getResources() {
            return resourceClasses;
        }


    }

    public class ResourceMirror {
        public Resource resource;
        public JDefinedClass valueClass;
        public JDefinedClass identifierClass;
        public Map<String, ViewMirror> viewClasses = Maps.newHashMap();


        private JFieldVar uriVar;

        public ResourceMirror(Resource resource, JDefinedClass valueClass, JDefinedClass identifierClass, JFieldVar uriVar, Map<String, ViewMirror> viewClasses) {
            this.resource = resource;
            this.valueClass = valueClass;
            this.identifierClass = identifierClass;
            this.uriVar = uriVar;
            this.viewClasses = viewClasses;
        }

        public String getName() {
            return resource.getName();
        }

        public JFieldRef getUriRef() {
            return valueClass.staticRef(uriVar);
        }

    }

    public class ViewMirror {
        public View view;
        public JDefinedClass viewClass;

        public ViewMirror(View view, JDefinedClass viewClass) {
            this.view = view;
            this.viewClass = viewClass;
        }

        public String getName() {
            return view.getName();
        }

    }


}
