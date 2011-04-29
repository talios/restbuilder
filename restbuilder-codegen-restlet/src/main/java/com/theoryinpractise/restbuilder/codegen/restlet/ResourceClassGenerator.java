package com.theoryinpractise.restbuilder.codegen.restlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.sun.codemodel.*;
import com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder;
import com.theoryinpractise.restbuilder.codegen.base.AbstractGenerator;
import com.theoryinpractise.restbuilder.codegen.base.ModelGenerator;
import com.theoryinpractise.restbuilder.parser.model.Identifier;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;

public class ResourceClassGenerator extends AbstractGenerator {

    private JCodeModel codeModel;
    private Model model;
    private JClass formRef;
    private JClass stringRef;
    private JFieldVar valueMediaType;
    private JClass stringRepresentationRef;

    public ResourceClassGenerator(JCodeModel codeModel, Model model) {
        this.codeModel = codeModel;
        this.model = model;
        this.formRef = codeModel.ref(Form.class);
        this.stringRef = codeModel.ref(String.class);
        this.stringRepresentationRef = codeModel.ref(StringRepresentation.class);
    }

    public JDefinedClass generateResourceClass(JPackage p, Model model, ModelGenerator.ResourceMirror resource) throws JClassAlreadyExistsException {
        String resourceName = camel(resource.getName() + "Resource");
        JDefinedClass resourceClass = p.subPackage("resource")._class(resourceName);
        resourceClass._extends(org.restlet.resource.Resource.class);
        resourceClass.javadoc().add("Top level REST resource - " + resource.getName());


        // org.restlet.data.MediaType.register()
        valueMediaType = resourceClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, codeModel.ref(MediaType.class), resource.getName().toUpperCase() + "_MEDIA_TYPE");
        valueMediaType
                .init(codeModel.ref(MediaType.class).staticInvoke("register")
                .arg(MediaTypeBuilder.buildContentType(model, resource.resource))
                .arg(camel(resource.getName()) + " Media Type"));


        JMethod constructor = resourceClass.constructor(JMod.PUBLIC);
        constructor.annotate(Inject.class);

        JVar context = constructor.param(Context.class, "context");
        JVar request = constructor.param(Request.class, "request");
        JVar response = constructor.param(Response.class, "response");

        constructor.body().invoke("super").arg(context).arg(request).arg(response);
        constructor.body().invoke("setVariants")
                .arg(codeModel.ref(Arrays.class).staticInvoke("asList")
                        .arg(JExpr._new(codeModel.ref(Variant.class))
                                .arg(codeModel.ref(MediaType.class).staticRef("APPLICATION_JSON"))));

        Map<String, JDefinedClass> resourceHandlerMap = generateHandlerClasses(p, resource.resource, resource.valueClass, resource.identifierClass);
        Map<String, JFieldVar> resourceHandlerFields = Maps.newHashMap();
        for (Map.Entry<String, JDefinedClass> entry : resourceHandlerMap.entrySet()) {
            JVar handler = constructor.param(entry.getValue(), entry.getKey() + "Handler");
            JFieldVar handlerField = resourceClass.field(JMod.PRIVATE | JMod.FINAL, entry.getValue(), "_" + entry.getKey() + "Handler");
            constructor.body().assign(handlerField, handler);
            resourceHandlerFields.put(entry.getKey(), handlerField);
        }

        JFieldVar mapper = resourceClass.field(
                JMod.PRIVATE | JMod.FINAL, codeModel.ref(ObjectMapper.class),
                "_mapper",
                JExpr._new(codeModel.ref(ObjectMapper.class)));




        JMethod allowGet = resourceClass.method(JMod.PUBLIC, codeModel.BOOLEAN, "allowGet");
        allowGet.annotate(Override.class);
        allowGet.body()._return(JExpr.lit(true));

        JMethod allowPost = resourceClass.method(JMod.PUBLIC, codeModel.BOOLEAN, "allowPost");
        allowPost.annotate(Override.class);
        allowPost.body()._return(JExpr.lit(true));

        JMethod identifierMethod = generateIdentifierGenerationMethod(resourceClass, resource.identifierClass, resource.resource);


        JMethod setResponseRepresentationGenerationMethod = setResponseRepresentationGenerationMethod(resourceClass);

        JMethod generateGetHeadersMethod = generateGetHeadersMethod(resourceClass);

        JMethod generateUpdateLinkHeadersMethod = generateUpdateLinkHeadersMethod(resourceClass, generateGetHeadersMethod);

        JMethod generateRepresentationGenerationMethod = generateRepresentationGenerationMethod(resourceClass, resource, mapper, generateUpdateLinkHeadersMethod);

        JMethod represent = resourceClass.method(JMod.PUBLIC, Representation.class, "represent");
        represent.annotate(Override.class);
        represent.param(Variant.class, "variant");
        represent._throws(ResourceException.class);

        JTryBlock jTryBlock = represent.body()._try();

        JInvocation representInvocation = resourceHandlerFields.get(resource.getName()).invoke("represent")
                .arg(JExpr.invoke(identifierMethod));


        jTryBlock.body()._return(JExpr.invoke(generateRepresentationGenerationMethod).arg(representInvocation));

        throwIoAsResource(codeModel, jTryBlock);

        JMethod post = resourceClass.method(JMod.PUBLIC, codeModel.VOID, "post");
        post.annotate(Override.class);
        JVar representation = post.param(JMod.FINAL, codeModel.ref(Representation.class), "representation");

        jTryBlock = post.body()._try();

        for (Operation operation : resource.resource.getOperations().values()) {
            JBlock block = makeIfBlockForOperation(jTryBlock.body(), representation, model, operation)._then();

            // Parse request content into operation value object
            // new ObjectMapper().readValue(r.getResponseBody(), Map.class);

            JVar operationModel = block.decl(
                    lookupOperationClass(operation),
                    operation.getName(),
                    mapper.invoke("readValue").arg(JExpr.invoke("getRequest").invoke("getEntity").invoke("getText")).arg(lookupOperationClass(operation).dotclass()));

            JInvocation handleInvocation = resourceHandlerFields.get(operation.getName()).invoke("handle" + camel(operation.getName()))
                    .arg(JExpr.invoke(identifierMethod))
                    .arg(operationModel);

            block.invoke(setResponseRepresentationGenerationMethod)
                    .arg(codeModel.ref(Status.class).staticRef("SUCCESS_OK"))
                    .arg(JExpr.invoke(generateRepresentationGenerationMethod).arg(handleInvocation));

        }

        setResponseStatus(jTryBlock.body(), null, codeModel.ref(Status.class).staticRef("CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE"));

        JCatchBlock catchBlock = jTryBlock._catch(codeModel.ref(IOException.class));
        JVar exceptionRef = catchBlock.param("e");
        setResponseStatus(
                catchBlock.body(),
                JExpr._new(stringRepresentationRef).arg(exceptionRef.invoke("getMessage")),
                codeModel.ref(Status.class).staticRef("SERVER_ERROR_INTERNAL"));

        catchBlock.body()._return();

        return resourceClass;
    }


    private JMethod generateUpdateLinkHeadersMethod(JDefinedClass resourceClass, JMethod generateGetHeadersMethod) {

        JMethod method = resourceClass.method(JMod.PRIVATE, codeModel.VOID, "updateLinkHeader");
        JVar response = method.param(codeModel.ref(Response.class), "response");
        JVar linkHeader = method.param(stringRef, "header");

        JVar headers = method.body().decl(formRef, "headers", JExpr.invoke(generateGetHeadersMethod).arg(response));

        JVar newLinkHeader = method.body().decl(stringRef, "newLinkHeader", JOp.cond(
                headers.invoke("getValuesMap").invoke("containsKey").arg("Link"),
                headers.invoke("getFirstValue").arg("Link").plus(JExpr.lit(", ")).plus(linkHeader),
                linkHeader
        ));

        method.body().add(headers.invoke("set").arg("Link").arg(newLinkHeader).arg(JExpr.FALSE));

        return method;

    }

    private JMethod generateGetHeadersMethod(JDefinedClass resourceClass) {

        JMethod method = resourceClass.method(JMod.PRIVATE, formRef, "getHeaders");
        JVar response = method.param(codeModel.ref(Response.class), "response");

        JVar headers = method.body().decl(formRef, "responseHeaders", JExpr.cast(formRef, response.invoke("getAttributes").invoke("get").arg("org.restlet.http.headers")));
        JBlock ifBlock = method.body()._if(headers.eq(JExpr._null()))._then();
        ifBlock.assign(headers, JExpr._new(formRef));
        ifBlock.add(response.invoke("getAttributes").invoke("put").arg("org.restlet.http.headers").arg(headers));

        method.body()._return(headers);


        return method;
    }


    private JMethod generateIdentifierGenerationMethod(JDefinedClass resourceClass, JDefinedClass valueClass, Resource resource) {

        JMethod identifierMethod = resourceClass.method(JMod.PRIVATE, valueClass, "generate" + camel(resource.getName()) + "Identifier");

        List<JVar> vars = Lists.newArrayList();
        for (Identifier identifier : resource.getIdentifiers()) {
            JClass type = resolveFieldType(codeModel, identifier);
            vars.add(identifierMethod.body().decl(type,
                    identifier.getName(),
                    type.staticInvoke("valueOf").arg(
                            JExpr.cast(stringRef,
                                    JExpr.invoke("getRequest").invoke("getAttributes").invoke("get").arg(identifier.getName().toLowerCase())))));
        }

        JInvocation identifierExpression = JExpr._new(valueClass);
        for (JVar var : vars) {
            identifierExpression.arg(var);
        }

        identifierMethod.body()._return(identifierExpression);

        return identifierMethod;

    }

    private JMethod generateRepresentationGenerationMethod(JDefinedClass resourceClass, ModelGenerator.ResourceMirror resource, JFieldVar mapper, JMethod generateUpdateLinkHeadersMethod) {

        JMethod method = resourceClass.method(
                JMod.PRIVATE,
                stringRepresentationRef,
                "generate" + camel(resource.getName()) + "Representation");
        method._throws(IOException.class);

        JVar value = method.param(JMod.FINAL, resource.valueClass, resource.getName());

        JVar representation = method.body().decl(
                stringRepresentationRef,
                "representation",
                JExpr._new(stringRepresentationRef)
                        .arg(mapper.invoke("writeValueAsString").arg(value)).arg(valueMediaType));

        for (Operation operation : resource.resource.getOperations().values()) {

            String operationLink = String.format("<%s>; rel=\"%s\"; title=\"%s\"; type=\"%s\"; method=\"%s\"",
                    resource.getUriRef(), operation.getName(), camel(operation.getName()), MediaTypeBuilder.buildContentType(model, operation), "POST");

            method.body().invoke(generateUpdateLinkHeadersMethod).arg(JExpr.invoke("getResponse")).arg(operationLink);

        }


        method.body()._return(representation);

        return method;

    }

    private JMethod setResponseRepresentationGenerationMethod(JDefinedClass resourceClass) {

        JMethod method = resourceClass.method(JMod.PRIVATE, codeModel.VOID, "setResponseRepresentation");
        JVar status = method.param(JMod.FINAL, codeModel.ref(Status.class), "status");
        JVar value = method.param(JMod.FINAL, stringRepresentationRef, "representation");

        setResponseStatus(method.body(), value, status);

        method.body()._return();

        return method;

    }

    private void setResponseStatus(JBlock block, JExpression newRepresentation, final JExpression status) {
        block.add(JExpr
                .invoke("getResponse")
                .invoke("setStatus")
                .arg(status));

        if (newRepresentation != null) {
            block.add(JExpr.invoke("getResponse").invoke("setEntity").arg(
                    newRepresentation));
        }

    }


    private Map<String, JDefinedClass> generateHandlerClasses(JPackage p, Resource resource, JDefinedClass res, JDefinedClass identifierClass) throws JClassAlreadyExistsException {

        Map<String, JDefinedClass> classMap = Maps.newHashMap();

        String handlerName = camel(resource.getName() + "Handler");
        JDefinedClass ifn = p.subPackage("handler")._interface(handlerName);
        ifn.method(JMod.NONE, res, "represent").param(identifierClass, "identifier");

        classMap.put(resource.getName(), ifn);

        for (Operation operation : resource.getOperations().values()) {
            String operationHandlerName = camel(resource.getName() + camel(operation.getName()) + "Handler");

            ifn = p.subPackage("handler")._interface(operationHandlerName);

            JMethod operationMethod = ifn.method(JMod.NONE, res, "handle" + camel(operation.getName()));
            operationMethod.param(JMod.FINAL, identifierClass, "identifier");
            operationMethod.param(JMod.FINAL, lookupOperationClass(operation), operation.getName());

            classMap.put(operation.getName(), ifn);
        }

        return classMap;
    }


    private JInvocation makeRepresentation(JFieldVar mapper, final JInvocation invoke) {
        return JExpr._new(stringRepresentationRef)
                .arg(mapper.invoke("writeValueAsString").arg(invoke));
    }

    private void throwIoAsResource(JCodeModel jCodeModel, JTryBlock jTryBlock) {
        JCatchBlock catchBlock = jTryBlock._catch(jCodeModel.ref(IOException.class));
        catchBlock.body()._throw(JExpr._new(jCodeModel.ref(ResourceException.class)).arg(catchBlock.param("e")));
    }

    private JConditional makeIfBlockForOperation(JBlock block, JVar representation, Model model, Operation operation) {
        return block._if(representation
                .invoke("getMediaType")
                .invoke("toString")
                .invoke("equals").arg(JExpr.lit(buildContentType(model, operation))));
    }

}
