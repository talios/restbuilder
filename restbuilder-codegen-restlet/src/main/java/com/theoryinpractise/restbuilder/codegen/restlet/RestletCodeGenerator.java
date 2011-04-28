package com.theoryinpractise.restbuilder.codegen.restlet;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.codegen.base.AbstractGenerator;
import com.theoryinpractise.restbuilder.codegen.base.ModelGenerator;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Resource;


/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 9/04/11
 * Time: 10:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RestletCodeGenerator extends AbstractGenerator implements CodeGenerator {

    private ModelGenerator modelGenerator = new ModelGenerator();

    public void generate(JCodeModel jCodeModel, Model model) throws JClassAlreadyExistsException {

        ModelGenerator.CodeGenModelMirror mirror = modelGenerator.mirrorOf(jCodeModel, model);

        modelGenerator.generateModelClasses(jCodeModel, mirror.getPackage(), model);

        generateResourceClasses(jCodeModel, mirror);

    }

    private void generateResourceClasses(JCodeModel jCodeModel, ModelGenerator.CodeGenModelMirror mirror) throws JClassAlreadyExistsException {

        ResourceClassGenerator resourceClassGenerator = new ResourceClassGenerator(jCodeModel, mirror.getModel());

        for (Resource resource : mirror.getModel().getResources().values()) {

            JDefinedClass valueClass = mirror.getResources().get(resource.getName());
            JDefinedClass identifierClass = mirror.getResourceIdentifiers().get(resource.getName());

            resourceClassGenerator.generateResourceClass(mirror.getPackage(), mirror.getModel(), resource, valueClass, identifierClass);

        }

    }

}
