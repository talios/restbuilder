package com.theoryinpractise.restbuilder.codegen.restlet;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder;
import com.theoryinpractise.restbuilder.codegen.base.AbstractGenerator;
import com.theoryinpractise.restbuilder.codegen.base.ModelGenerator;
import com.theoryinpractise.restbuilder.parser.BaseClassElement;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import org.restlet.Finder;
import org.restlet.Router;
import org.restlet.data.MediaType;

import java.util.Map;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;


/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 9/04/11
 * Time: 10:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RestletCodeGenerator extends AbstractGenerator implements CodeGenerator {

    private ModelGenerator modelGenerator = new ModelGenerator();
    private Map<String, JFieldVar> mediaTypeVars = Maps.newHashMap();

    public void generate(JCodeModel jCodeModel, Model model) throws JClassAlreadyExistsException {

        ModelGenerator.CodeGenModelMirror mirror = modelGenerator.mirrorOf(jCodeModel, model);

//        modelGenerator.generateModelClasses(jCodeModel, mirror.getPackage(), model);

        generateResourceClasses(jCodeModel, mirror);


    }

    private void generateResourceClasses(JCodeModel codeModel, ModelGenerator.CodeGenModelMirror mirror) throws JClassAlreadyExistsException {

        Model model = mirror.getModel();

        JDefinedClass routerClass = mirror.getPackage()._class(camel(model.getNamespace()) + "RouteManager");

        JMethod attachMethod = routerClass.method(
                JMod.PUBLIC | JMod.STATIC, codeModel.VOID,
                "attach" + camel(model.getNamespace()) + "Model");
        JVar router = attachMethod.param(JMod.FINAL, codeModel.ref(Router.class), "router");

        JVar finderFunction = attachMethod.param(
                JMod.FINAL,
                codeModel.ref(Function.class).narrow(
                        codeModel.ref(Class.class).narrow(codeModel.ref(org.restlet.resource.Resource.class).wildcard()),
                        codeModel.ref(Finder.class)
                ), "finder");




        ResourceClassGenerator resourceClassGenerator = new ResourceClassGenerator(codeModel, model);

        for (ModelGenerator.ResourceMirror resourceMirror : mirror.getResources().values()) {

            JFieldVar valueMediaType = defineMediaTypeVar(codeModel, model, routerClass, resourceMirror.resource);
            mediaTypeVars.put(buildContentType(model, resourceMirror.resource), valueMediaType);

            // generate view media types
            for (ModelGenerator.ViewMirror viewMirror : resourceMirror.viewClasses.values()) {
                JFieldVar vieweMediaType = defineMediaTypeVar(codeModel, model, routerClass, viewMirror.view);
                mediaTypeVars.put(buildContentType(model, viewMirror.view), vieweMediaType);
            }

            // generate operation media types
            for (Operation operation : resourceMirror.resource.getOperations().values()) {
                JFieldVar var = defineMediaTypeVar(codeModel, model, routerClass, operation);
                mediaTypeVars.put(buildContentType(model, operation), var);
            }


            JDefinedClass resourceClass = resourceClassGenerator.generateResourceClass(mirror.getPackage(), model, resourceMirror,
                    new MediaTypeVarContainer(routerClass, model));

            attachMethod.body().add(router.invoke("attach")
                    .arg(resourceMirror.getUriRef())
                    .arg(finderFunction.invoke("apply").arg(resourceClass.dotclass())));

        }

    }

    public class MediaTypeVarContainer {

        private Model model;
        private JDefinedClass routerClass;

        public MediaTypeVarContainer(JDefinedClass routerClass, Model model) {
            this.routerClass  = routerClass;
            this.model = model;
        }

        public JFieldRef getMediaType(BaseClassElement element) {
            return routerClass.staticRef(mediaTypeVars.get(buildContentType(model, element)));
        }
    }

    private JFieldVar defineMediaTypeVar(JCodeModel codeModel, Model model, JDefinedClass resourceClass, BaseClassElement element) {
        JFieldVar mediaType = resourceClass.field(
                JMod.PUBLIC | JMod.STATIC | JMod.FINAL,
                codeModel.ref(MediaType.class),
                element.getMediaTypeName().toUpperCase() + "_MEDIA_TYPE");

        mediaType
                .init(codeModel.ref(MediaType.class).staticInvoke("register")
                        .arg(MediaTypeBuilder.buildContentType(model, element))
                        .arg(camel(element.getMediaTypeName()) + " Media Type"));

        return mediaType;
    }

}
