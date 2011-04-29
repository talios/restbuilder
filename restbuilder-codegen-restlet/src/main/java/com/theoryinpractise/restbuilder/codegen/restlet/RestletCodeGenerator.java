package com.theoryinpractise.restbuilder.codegen.restlet;

import com.google.common.base.Function;
import com.sun.codemodel.*;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.codegen.base.AbstractGenerator;
import com.theoryinpractise.restbuilder.codegen.base.ModelGenerator;
import com.theoryinpractise.restbuilder.parser.model.Model;
import org.restlet.Finder;
import org.restlet.Router;


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

//        modelGenerator.generateModelClasses(jCodeModel, mirror.getPackage(), model);

        generateResourceClasses(jCodeModel, mirror);


    }

    private void generateResourceClasses(JCodeModel codeModel, ModelGenerator.CodeGenModelMirror mirror) throws JClassAlreadyExistsException {

        JDefinedClass routerClass = mirror.getPackage()._class(camel(mirror.getModel().getNamespace()) + "RouteManager");

        JMethod attachMethod = routerClass.method(
                JMod.PUBLIC | JMod.STATIC, codeModel.VOID,
                "attach" + camel(mirror.getModel().getNamespace()) + "Model");
        JVar router = attachMethod.param(JMod.FINAL, codeModel.ref(Router.class), "router");

        JVar finderFunction = attachMethod.param(
                JMod.FINAL,
                codeModel.ref(Function.class).narrow(
                        codeModel.ref(Class.class).narrow(codeModel.ref(org.restlet.resource.Resource.class).wildcard()),
                        codeModel.ref(Finder.class)
                ), "finder");


        ResourceClassGenerator resourceClassGenerator = new ResourceClassGenerator(codeModel, mirror.getModel());

        for (ModelGenerator.ResourceMirror resourceMirror : mirror.getResources().values()) {


            JDefinedClass resourceClass = resourceClassGenerator.generateResourceClass(mirror.getPackage(), mirror.getModel(), resourceMirror);

            // router.attach(path, new GuiceFinder(resourceClass));

            attachMethod.body().add(router.invoke("attach")
                    .arg(resourceMirror.getUriRef())
                    .arg(finderFunction.invoke("apply").arg(resourceClass.dotclass())));

        }

    }

}
