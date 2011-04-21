package com.theoryinpractise.restbuilder.codegen.restlet;

import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.parser.model.RestAttribute;
import com.theoryinpractise.restbuilder.parser.model.RestModel;
import com.theoryinpractise.restbuilder.parser.model.RestOperation;
import com.theoryinpractise.restbuilder.parser.model.RestResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;


/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 9/04/11
 * Time: 10:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RestletCodeGenerator implements CodeGenerator {

    public static final String OPERATION = "Operation";
    private Map<String, JDefinedClass> definedClasses = Maps.newHashMap();

    public void generate(JCodeModel jCodeModel, RestModel model) throws JClassAlreadyExistsException {

        JPackage aPackage = jCodeModel._package(model.getPackage());

        generateOperationModelClasses(jCodeModel, aPackage, model);

        generateOperationResourceClasses(jCodeModel, aPackage, model);


    }

    private void generateOperationResourceClasses(JCodeModel jCodeModel, JPackage p, RestModel model) throws JClassAlreadyExistsException {

        for (RestResource resource : model.getResources()) {

            String name = camel(resource.getName());
            String handlerName = camel(resource.getName() + "Handler");
            String resourceName = camel(resource.getName() + "Resource");

            JDefinedClass res = generateImmutableBean(jCodeModel, p, name, resource.getAttributes());

            for (RestOperation restOperation : resource.getOperations()) {
                generateImmutableBean(jCodeModel, p.subPackage("operation"), makeOperationClassName(restOperation), restOperation.getAttributes());
            }


            JDefinedClass ifn = p.subPackage("handler")._interface(handlerName);
            ifn.method(JMod.NONE, res, "represent");
            for (RestOperation operation : resource.getOperations()) {
                ifn.method(JMod.NONE, res, "handle" + camel(operation.getName())).param(JMod.FINAL, lookupOperationClass(operation), operation.getName());
            }

            JDefinedClass jc = p.subPackage("resource")._class(resourceName);
            jc._extends(org.restlet.resource.Resource.class);
            jc.javadoc().add("Top level REST resource - " + resource.getName());

            JMethod constructor = jc.constructor(JMod.PUBLIC);
            JVar context = constructor.param(Context.class, "context");
            JVar request = constructor.param(Request.class, "request");
            JVar response = constructor.param(Response.class, "response");
            JVar handler = constructor.param(ifn, "Handler");
            JFieldVar handlerField = jc.field(JMod.PRIVATE | JMod.FINAL, ifn, "_handler");
            JFieldVar mapper = jc.field(JMod.PRIVATE | JMod.FINAL, jCodeModel.ref(ObjectMapper.class), "_mapper");
            mapper.init(JExpr._new(jCodeModel.ref(ObjectMapper.class)));

            constructor.body().invoke("super").arg(context).arg(request).arg(response);
            constructor.body().invoke("setVariants")
                    .arg(jCodeModel.ref(Arrays.class).staticInvoke("asList").arg(JExpr._new(jCodeModel.ref(Variant.class)).arg(jCodeModel.ref(MediaType.class).staticRef("APPLICATION_JSON"))));

            constructor.body().assign(handlerField, handler);

            JMethod allowGet = jc.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "allowGet");
            allowGet.annotate(Override.class);
            allowGet.body()._return(JExpr.lit(true));

            JMethod allowPost = jc.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "allowPost");
            allowPost.annotate(Override.class);
            allowPost.body()._return(JExpr.lit(true));


            JMethod represent = jc.method(JMod.PUBLIC, Representation.class, "represent");
            represent.annotate(Override.class);
            represent.param(Variant.class, "variant");
            represent._throws(ResourceException.class);

            JTryBlock jTryBlock = represent.body()._try();
            jTryBlock.body()._return(makeRepresentation(jCodeModel, mapper, handlerField.invoke("represent")));
            throwIoAsResource(jCodeModel, jTryBlock);

            JMethod post = jc.method(JMod.PUBLIC, jCodeModel.VOID, "post");
            post.annotate(Override.class);
            JVar representation = post.param(JMod.FINAL, jCodeModel.ref(Representation.class), "representation");

            jTryBlock = post.body()._try();

            for (RestOperation operation : resource.getOperations()) {
                JBlock block = makeIfBlockForOperation(jTryBlock, representation, model, operation)._then();
                JVar operationModel = block.decl(lookupOperationClass(operation), operation.getName(), JExpr._null());
                JInvocation newRepresentation = makeRepresentation(jCodeModel, mapper, handlerField.invoke("handle" + camel(operation.getName())).arg(operationModel));
                block.add(JExpr.invoke("getResponse").invoke("setEntity").arg(
                        newRepresentation));

            }
            throwIoAsResource(jCodeModel, jTryBlock);
        }

    }

    private void throwIoAsResource(JCodeModel jCodeModel, JTryBlock jTryBlock) {
        JCatchBlock catchBlock = jTryBlock._catch(jCodeModel.ref(IOException.class));
        catchBlock.body()._throw(JExpr._new(jCodeModel.ref(ResourceException.class)).arg(catchBlock.param("e")));
    }

    private JDefinedClass lookupOperationClass(RestOperation operation) {
        return definedClasses.get(makeOperationClassName(operation));
    }

    private JInvocation makeRepresentation(JCodeModel jCodeModel, JFieldVar mapper, final JInvocation invoke) {
        return JExpr._new(jCodeModel.ref(StringRepresentation.class))
                .arg(mapper.invoke("writeValueAsString").arg(invoke));
    }

    private JConditional makeIfBlockForOperation(JTryBlock block, JVar representation, RestModel model, RestOperation operation) {
        return block.body()._if(representation
                .invoke("getMediaType")
                .invoke("toString")
                .invoke("equals").arg(JExpr.lit(buildContentType(model, operation))));
    }

    private void generateOperationModelClasses(JCodeModel jCodeModel, JPackage p, RestModel model) throws JClassAlreadyExistsException {
        for (RestOperation restOperation : model.getOperations()) {
            generateImmutableBean(jCodeModel, p.subPackage("operation"), makeOperationClassName(restOperation), restOperation.getAttributes());
        }
    }

    private String makeOperationClassName(RestOperation restOperation) {
        return camel(restOperation.getName() + OPERATION);
    }

    private JDefinedClass generateImmutableBean(JCodeModel jCodeModel,
                                                JPackage p,
                                                final String className,
                                                final List<RestAttribute> attributes) throws JClassAlreadyExistsException {

        if (definedClasses.containsKey(className)) {
            return definedClasses.get(className);
        }

        JDefinedClass jc = p._class(className);
//        jc.javadoc().add("Top level REST operation - " + restOperation.getName());

        JMethod constructor = jc.constructor(JMod.PUBLIC);
        constructor.javadoc().add("Create a new instance of the operation class");
        JBlock body = constructor.body();
        for (RestAttribute attr : attributes) {
            JFieldVar field = jc.field(JMod.PRIVATE | JMod.FINAL, resolveAttributeType(jCodeModel, attr), "_" + attr.getAttributeName());
            JVar param = constructor.param(com.sun.codemodel.internal.JMod.FINAL, resolveAttributeType(jCodeModel, attr), attr.getAttributeName());
            constructor.javadoc().addParam(attr.getAttributeName()).add("some comment");
            body.assign(field, param);

            JMethod getter = jc.method(JMod.PUBLIC, resolveAttributeType(jCodeModel, attr), "get" + camel(attr.getAttributeName()));
            getter.body()._return(field);
            getter.javadoc().add("Return the content of the " + attr.getAttributeName() + " attribute.");
        }

        definedClasses.put(className, jc);
        return jc;
    }

    private JClass resolveAttributeType(JCodeModel jCodeModel, RestAttribute attr) {
        if ("string".equals(attr.getAttributeType())) {
            return jCodeModel.ref(String.class);
        }
        if ("datetime".equals(attr.getAttributeType())) {
            return jCodeModel.ref(Date.class);
        }
        if ("integer".equals(attr.getAttributeType())) {
            return jCodeModel.ref(Integer.class);
        }
        if ("double".equals(attr.getAttributeType())) {
            return jCodeModel.ref(Double.class);
        }
        throw new IllegalArgumentException("Unsupported attribute type: " + attr.getAttributeType());
    }


    private static String camel(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}
