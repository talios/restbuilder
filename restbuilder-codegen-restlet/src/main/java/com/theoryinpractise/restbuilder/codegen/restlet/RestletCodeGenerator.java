package com.theoryinpractise.restbuilder.codegen.restlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.sun.codemodel.*;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.parser.model.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
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

    public void generate(JCodeModel jCodeModel, Model model) throws JClassAlreadyExistsException {

        JPackage aPackage = jCodeModel._package(model.getPackage());

        generateModelClasses(jCodeModel, aPackage, model);

        generateResourceClasses(jCodeModel, aPackage, model);


    }

    private void generateResourceClasses(JCodeModel jCodeModel, JPackage p, Model model) throws JClassAlreadyExistsException {

        for (Resource resource : model.getResources()) {

            JDefinedClass valueClass = generateValueClass(jCodeModel, p, resource);

            JDefinedClass identifierClass = generateIdentifierClass(jCodeModel, p, resource);

            generateOperationClasses(jCodeModel, p, resource);


            String resourceName = camel(resource.getName() + "Resource");
            JDefinedClass resourceClass = p.subPackage("resource")._class(resourceName);
            resourceClass._extends(org.restlet.resource.Resource.class);
            resourceClass.javadoc().add("Top level REST resource - " + resource.getName());

            JMethod constructor = resourceClass.constructor(JMod.PUBLIC);
            constructor.annotate(Inject.class);

            JVar context = constructor.param(Context.class, "context");
            JVar request = constructor.param(Request.class, "request");
            JVar response = constructor.param(Response.class, "response");

            constructor.body().invoke("super").arg(context).arg(request).arg(response);
                        constructor.body().invoke("setVariants")
                                .arg(jCodeModel.ref(Arrays.class).staticInvoke("asList")
                                        .arg(JExpr._new(jCodeModel.ref(Variant.class))
                                                .arg(jCodeModel.ref(MediaType.class).staticRef("APPLICATION_JSON"))));

            Map<String, JDefinedClass> resourceHandlerMap = generateHandlerClasses(p, resource, valueClass, identifierClass);
            Map<String, JFieldVar> resourceHandlerFields = Maps.newHashMap();
            for (Map.Entry<String, JDefinedClass> entry : resourceHandlerMap.entrySet()) {
                JVar handler = constructor.param(entry.getValue(), entry.getKey() + "Handler");
                JFieldVar handlerField = resourceClass.field(JMod.PRIVATE | JMod.FINAL, entry.getValue(), "_" + entry.getKey() + "Handler");
                constructor.body().assign(handlerField, handler);
                resourceHandlerFields.put(entry.getKey(), handlerField);
            }

            JFieldVar mapper = resourceClass.field(
                    JMod.PRIVATE | JMod.FINAL, jCodeModel.ref(ObjectMapper.class),
                    "_mapper",
                    JExpr._new(jCodeModel.ref(ObjectMapper.class)));


            // URI constant
            StringBuilder sb = new StringBuilder("/" + resource.getName());
            if (!resource.getIdentifiers().isEmpty()) {
                for (Identifier identifier : resource.getIdentifiers()) {
                    sb.append("/{").append(identifier.getName()).append("}");
                }
            }
            resourceClass.field(JMod.PUBLIC | JMod.FINAL | JMod.STATIC, jCodeModel.ref(String.class),
                    "URI", JExpr.lit(sb.toString()));



            JMethod allowGet = resourceClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "allowGet");
            allowGet.annotate(Override.class);
            allowGet.body()._return(JExpr.lit(true));

            JMethod allowPost = resourceClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "allowPost");
            allowPost.annotate(Override.class);
            allowPost.body()._return(JExpr.lit(true));

            JMethod identifierMethod = generateIdentifierGenerationMethod(jCodeModel, resourceClass, identifierClass, resource);

            JMethod generateRepresentationGenerationMethod = generateRepresentationGenerationMethod(jCodeModel, resourceClass, valueClass, resource, mapper);

            JMethod setResponseRepresentationGenerationMethod = setResponseRepresentationGenerationMethod(jCodeModel, resourceClass);



            JMethod represent = resourceClass.method(JMod.PUBLIC, Representation.class, "represent");
            represent.annotate(Override.class);
            represent.param(Variant.class, "variant");
            represent._throws(ResourceException.class);

            JTryBlock jTryBlock = represent.body()._try();

            JInvocation representInvocation = resourceHandlerFields.get(resource.getName()).invoke("represent")
                    .arg(JExpr.invoke(identifierMethod));


            jTryBlock.body()._return(JExpr.invoke(generateRepresentationGenerationMethod).arg(representInvocation));

            throwIoAsResource(jCodeModel, jTryBlock);

            JMethod post = resourceClass.method(JMod.PUBLIC, jCodeModel.VOID, "post");
            post.annotate(Override.class);
            JVar representation = post.param(JMod.FINAL, jCodeModel.ref(Representation.class), "representation");

            jTryBlock = post.body()._try();

            for (Operation operation : resource.getOperations()) {
                JBlock block = makeIfBlockForOperation(jTryBlock.body(), representation, model, operation)._then();

                // Parse request content into operation value object
                // new ObjectMapper().readValue(r.getResponseBody(), Map.class);
                JVar operationModel = block.decl(
                        lookupOperationClass(operation),
                        operation.getName(),
                        mapper.invoke("readValue").arg("").arg(lookupOperationClass(operation).dotclass()));

                JInvocation handleInvocation = resourceHandlerFields.get(operation.getName()).invoke("handle" + camel(operation.getName()))
                        .arg(JExpr.invoke(identifierMethod))
                        .arg(operationModel);

                block.invoke(setResponseRepresentationGenerationMethod)
                        .arg(jCodeModel.ref(Status.class).staticRef("SUCCESS_OK"))
                        .arg(JExpr.invoke(generateRepresentationGenerationMethod).arg(handleInvocation));

            }

            JCatchBlock catchBlock = jTryBlock._catch(jCodeModel.ref(IOException.class));
            setResponseStatus(catchBlock.body(),  null, jCodeModel.ref(Status.class).staticRef("CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE"));
            catchBlock.body()._return();

        }


    }

    private JMethod generateIdentifierGenerationMethod(JCodeModel jCodeModel,JDefinedClass resourceClass,  JDefinedClass valueClass, Resource resource) {

        JMethod identifierMethod = resourceClass.method(JMod.PRIVATE, valueClass, "generate" + camel(resource.getName()) + "Identifier");

        List < JVar > vars = Lists.newArrayList();
        for (Identifier identifier : resource.getIdentifiers()) {
            JClass type = resolveFieldType(jCodeModel, identifier);
            vars.add(identifierMethod.body().decl(type,
                    identifier.getName(),
                    type.staticInvoke("valueOf").arg(
                            JExpr.cast(jCodeModel.ref(String.class),
                                    JExpr.invoke("getRequest").invoke("getAttributes").invoke("get").arg(identifier.getName().toLowerCase())))));
        }

        JInvocation identifierExpression = JExpr._new(valueClass);
        for (JVar var : vars) {
            identifierExpression.arg(var);
        }

        identifierMethod.body()._return(identifierExpression);

        return identifierMethod;

    }

    private JMethod generateRepresentationGenerationMethod(JCodeModel jCodeModel, JDefinedClass resourceClass, JDefinedClass valueClass, Resource resource, JFieldVar mapper) {

        JMethod method = resourceClass.method(
                JMod.PRIVATE,
                jCodeModel.ref(StringRepresentation.class),
                "generate" + camel(resource.getName()) + "Representation");
        method._throws(IOException.class);

        JVar value = method.param(JMod.FINAL, valueClass, resource.getName());

        JVar representation = method.body().decl(
                jCodeModel.ref(StringRepresentation.class),
                "representation",
                JExpr._new(jCodeModel.ref(StringRepresentation.class))
                        .arg(mapper.invoke("writeValueAsString").arg(value)));


        method.body()._return(representation);

        return method;

    }

    private JMethod setResponseRepresentationGenerationMethod(JCodeModel jCodeModel, JDefinedClass resourceClass) {

        JMethod method = resourceClass.method(JMod.PRIVATE, jCodeModel.VOID, "setResponseRepresentation");
        JVar status = method.param(JMod.FINAL, jCodeModel.ref(Status.class), "status");
        JVar value = method.param(JMod.FINAL, jCodeModel.ref(StringRepresentation.class), "representation");

        setResponseStatus(method.body(), value, status);

        method.body()._return();

        return method;

    }

    private void setResponseStatus(JBlock block, JVar newRepresentation, final JExpression status) {
        block.add(JExpr
                .invoke("getResponse")
                .invoke("setStatus")
                .arg(status));

        if (newRepresentation != null) {
            block.add(JExpr.invoke("getResponse").invoke("setEntity").arg(
                    newRepresentation));
        }

    }


    private JInvocation makeRepresentation(JCodeModel jCodeModel, JFieldVar mapper, final JInvocation invoke) {
        return JExpr._new(jCodeModel.ref(StringRepresentation.class))
                .arg(mapper.invoke("writeValueAsString").arg(invoke));
    }


    private JDefinedClass generateValueClass(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        String name = camel(resource.getName());
        return generateImmutableBean(jCodeModel, p, name, resource.getFields());
    }

    private void generateOperationClasses(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        for (Operation operation : resource.getOperations()) {
            generateImmutableBean(jCodeModel, p.subPackage("operation"), makeOperationClassName(operation), operation.getAttributes());
        }
    }

    private JDefinedClass generateIdentifierClass(JCodeModel jCodeModel, JPackage p, Resource resource) throws JClassAlreadyExistsException {
        String identifierName = camel(resource.getName() + "Identifier");
        return generateImmutableBean(jCodeModel, p, identifierName, resource.getIdentifiers());
    }

    private Map<String,JDefinedClass> generateHandlerClasses(JPackage p, Resource resource, JDefinedClass res, JDefinedClass identifierClass) throws JClassAlreadyExistsException {

        Map<String,JDefinedClass> classMap = Maps.newHashMap();

        String handlerName = camel(resource.getName() + "Handler");
        JDefinedClass ifn = p.subPackage("handler")._interface(handlerName);
        ifn.method(JMod.NONE, res, "represent").param(identifierClass, "identifier");

        classMap.put(resource.getName(), ifn);

        for (Operation operation : resource.getOperations()) {
            String operationHandlerName = camel(resource.getName() + camel(operation.getName()) +  "Handler");

            ifn = p.subPackage("handler")._interface(operationHandlerName);

            JMethod operationMethod = ifn.method(JMod.NONE, res, "handle" + camel(operation.getName()));
            operationMethod.param(JMod.FINAL, identifierClass, "identifier");
            operationMethod.param(JMod.FINAL, lookupOperationClass(operation), operation.getName());

            classMap.put(operation.getName(), ifn);
        }

        return classMap;
    }

    private void throwIoAsResource(JCodeModel jCodeModel, JTryBlock jTryBlock) {
        JCatchBlock catchBlock = jTryBlock._catch(jCodeModel.ref(IOException.class));
        catchBlock.body()._throw(JExpr._new(jCodeModel.ref(ResourceException.class)).arg(catchBlock.param("e")));
    }

    private JDefinedClass lookupOperationClass(Operation operation) {
        return definedClasses.get(makeOperationClassName(operation));
    }

    private JConditional makeIfBlockForOperation(JBlock block, JVar representation, Model model, Operation operation) {
        return block._if(representation
                .invoke("getMediaType")
                .invoke("toString")
                .invoke("equals").arg(JExpr.lit(buildContentType(model, operation))));
    }

    private void generateModelClasses(JCodeModel jCodeModel, JPackage p, Model model) throws JClassAlreadyExistsException {
        for (Operation operation : model.getOperations()) {
            generateImmutableBean(jCodeModel, p.subPackage("operation"), makeOperationClassName(operation), operation.getAttributes());
        }
    }

    private String makeOperationClassName(Operation operation) {
        return camel(operation.getName() + OPERATION);
    }

    private JDefinedClass generateImmutableBean(JCodeModel jCodeModel,
                                                JPackage p,
                                                final String className,
                                                final List<? extends Field> fields) throws JClassAlreadyExistsException {

        if (definedClasses.containsKey(className)) {
            return definedClasses.get(className);
        }

        JDefinedClass jc = p._class(className);
//        jc.javadoc().add("Top level REST operation - " + restOperation.getName());

        JMethod constructor = jc.constructor(JMod.PUBLIC);
        constructor.javadoc().add("Create a new instance of the class");
        JBlock body = constructor.body();
        for (Field attr : fields) {
            JFieldVar field = jc.field(JMod.PRIVATE | JMod.FINAL, resolveFieldType(jCodeModel, attr), "_" + attr.getName());
            JVar param = constructor.param(com.sun.codemodel.internal.JMod.FINAL, resolveFieldType(jCodeModel, attr), attr.getName());
            constructor.javadoc().addParam(attr.getName()).add("some comment");
            body.assign(field, param);

            JMethod getter = jc.method(JMod.PUBLIC, resolveFieldType(jCodeModel, attr), "get" + camel(attr.getName()));
            getter.body()._return(field);
            getter.javadoc().add("Return the content of the " + attr.getName() + " attribute.");
        }

        definedClasses.put(className, jc);
        return jc;
    }

    private JClass resolveFieldType(JCodeModel jCodeModel, Field attr) {
        if ("string".equals(attr.getType())) {
            return jCodeModel.ref(String.class);
        }
        if ("datetime".equals(attr.getType())) {
            return jCodeModel.ref(Date.class);
        }
        if ("integer".equals(attr.getType())) {
            return jCodeModel.ref(Integer.class);
        }
        if ("double".equals(attr.getType())) {
            return jCodeModel.ref(Double.class);
        }
        throw new IllegalArgumentException("Unsupported attribute type: " + attr.getType());
    }


    private static String camel(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}
