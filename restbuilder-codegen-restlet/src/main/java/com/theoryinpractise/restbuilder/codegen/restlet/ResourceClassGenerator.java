package com.theoryinpractise.restbuilder.codegen.restlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.sun.codemodel.*;
import com.theoryinpractise.restbuilder.codegen.base.AbstractGenerator;
import com.theoryinpractise.restbuilder.codegen.base.ModelGenerator;
import com.theoryinpractise.restbuilder.parser.MediaTypeElement;
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

import static com.sun.codemodel.JExpr.lit;

public class ResourceClassGenerator extends AbstractGenerator {

    private JCodeModel codeModel;
    private Model model;
    private JClass formRef;
    private JClass stringRef;
    private JClass stringRepresentationRef;
    private RestletCodeGenerator.MediaTypeVarContainer mediaTypes;

    public ResourceClassGenerator(JCodeModel codeModel, Model model) {
        this.codeModel = codeModel;
        this.model = model;
        this.formRef = codeModel.ref(Form.class);
        this.stringRef = codeModel.ref(String.class);
        this.stringRepresentationRef = codeModel.ref(StringRepresentation.class);
    }

    public JDefinedClass generateResourceClass(JPackage p, Model model, ModelGenerator.ResourceMirror resourceMirror, RestletCodeGenerator.MediaTypeVarContainer mediaTypes) throws JClassAlreadyExistsException {
        String resourceName = camel(resourceMirror.getName() + "Resource");
        JDefinedClass resourceClass = p.subPackage("resource")._class(resourceName);
        resourceClass._extends(org.restlet.resource.Resource.class);
        resourceClass.javadoc().add("Top level REST resourceMirror - " + resourceMirror.getName());
        this.mediaTypes = mediaTypes;


        // org.restlet.data.MediaType.register()


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

        Map<String, JDefinedClass> resourceHandlerMap = generateHandlerClasses(p, resourceMirror);

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

        JMethod identifierMethod = generateIdentifierGenerationMethod(resourceClass, resourceMirror.identifierClass, resourceMirror.resource);


        JMethod setResponseRepresentationGenerationMethod = setResponseRepresentationGenerationMethod(resourceClass);

        JMethod generateGetHeadersMethod = generateGetHeadersMethod(resourceClass);

        JMethod generateUpdateLinkHeadersMethod = generateUpdateLinkHeadersMethod(resourceClass, generateGetHeadersMethod);

        JMethod generateRepresentationGenerationMethod = generateRepresentationGenerationMethod(resourceClass, resourceMirror, mapper, generateUpdateLinkHeadersMethod);

        generateRepresentMethod(resourceMirror, resourceClass, resourceHandlerFields, identifierMethod, generateRepresentationGenerationMethod);


        JMethod post = resourceClass.method(JMod.PUBLIC, codeModel.VOID, "post");
        post.annotate(Override.class);
        JVar representation = post.param(JMod.FINAL, codeModel.ref(Representation.class), "representation");

        JTryBlock jTryBlock = post.body()._try();

        for (Operation operation : resourceMirror.resource.getOperations().values()) {
            JBlock block = makeIfBlockForMediaTypeElement(jTryBlock.body(), representation, operation)._then();

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
                    .arg(JExpr.invoke(generateRepresentationGenerationMethod).arg(handleInvocation).arg(mediaTypes.getMediaType(resourceMirror.resource)));

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

    private void generateRepresentMethod(ModelGenerator.ResourceMirror resource,
                                         JDefinedClass resourceClass,
                                         Map<String, JFieldVar> resourceHandlerFields,
                                         JMethod identifierMethod,
                                         JMethod generateRepresentationGenerationMethod) {

        JMethod represent = resourceClass.method(JMod.PUBLIC, Representation.class, "represent");
        represent.annotate(Override.class);
        JVar variantVar = represent.param(Variant.class, "variant");
        represent._throws(ResourceException.class);

        JTryBlock jTryBlock = represent.body()._try();

        for (ModelGenerator.ViewMirror viewMirror : resource.viewClasses.values()) {

            JConditional ifBlock = makeIfBlockForMediaTypeElement(jTryBlock.body(), variantVar, viewMirror.view);

            JInvocation representInvocation = resourceHandlerFields.get(viewMirror.view.getMediaTypeName()).invoke("represent")
                    .arg(JExpr.invoke(identifierMethod));

            ifBlock._then()._return(JExpr.invoke(generateRepresentationGenerationMethod)
                    .arg(representInvocation)
                    .arg(mediaTypes.getMediaType(viewMirror.view)));

        }

        JInvocation representInvocation = resourceHandlerFields.get(resource.resource.getMediaTypeName()).invoke("represent")
                .arg(JExpr.invoke(identifierMethod));


        jTryBlock.body()._return(JExpr.invoke(generateRepresentationGenerationMethod)
                .arg(representInvocation)
                .arg(mediaTypes.getMediaType(resource.resource)));

        throwIoAsResource(codeModel, jTryBlock);
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

        JVar value = method.param(JMod.FINAL, codeModel.ref(Object.class), resource.getName());
        JVar mediaType = method.param(JMod.FINAL, codeModel.ref(MediaType.class), "mediaType");

        JVar representation = method.body().decl(
                stringRepresentationRef,
                "representation",
                JExpr._new(stringRepresentationRef)
                        .arg(mapper.invoke("writeValueAsString").arg(value)).arg(mediaType));



        for (Operation operation : resource.resource.getOperations().values()) {

            method.body().invoke(generateUpdateLinkHeadersMethod)
                    .arg(resource.getUriRef())
                    .arg(lit("operation " + operation.getName().toLowerCase()))
                    .arg(lit(camel(operation.getName())))
                    .arg(mediaTypes.getMediaType(operation))
                    .arg(codeModel.ref(Method.class).staticRef("POST"));

        }

        for (ModelGenerator.ViewMirror viewMirror : resource.viewClasses.values()) {

            method.body().invoke(generateUpdateLinkHeadersMethod)
                    .arg(resource.getUriRef())
                    .arg(lit("view " + viewMirror.getName().toLowerCase()))
                    .arg(lit(camel(viewMirror.getName())))
                    .arg(mediaTypes.getMediaType(viewMirror.view))
                    .arg(codeModel.ref(Method.class).staticRef("GET"));

        }


        method.body()._return(representation);

        return method;

    }

    private JMethod generateUpdateLinkHeadersMethod(JDefinedClass resourceClass, JMethod generateGetHeadersMethod) {

        JMethod method = resourceClass.method(JMod.PRIVATE, codeModel.VOID, "updateLinkHeader");
//        JVar linkHeader = method.param(stringRef, "header");
        JVar uriVar = method.param(stringRef, "url");
        JVar relVar = method.param(stringRef, "rel");
        JVar titleVar = method.param(stringRef, "title");
        JVar mediaTypeVar = method.param(codeModel.ref(MediaType.class), "mediaType");
        JVar methodVar = method.param(codeModel.ref(Method.class), "method");

        JVar headerVar = method.body().decl(stringRef, "header", stringRef.staticInvoke("format")
                .arg(lit("<%s>; rel=\"%s\"; title=\"%s\"; type=\"%s\"; method=\"%s\""))
                .arg(uriVar)
                .arg(relVar)
                .arg(titleVar)
                .arg(mediaTypeVar.invoke("getName"))
                .arg(methodVar.invoke("getName")));


        JVar headers = method.body().decl(formRef, "headers", JExpr.invoke(generateGetHeadersMethod).arg(JExpr.invoke("getResponse")));

        JVar newLinkHeader = method.body().decl(stringRef, "newLinkHeader", JOp.cond(
                headers.invoke("getValuesMap").invoke("containsKey").arg("Link"),
                headers.invoke("getFirstValue").arg("Link").plus(JExpr.lit(", ")).plus(headerVar),
                headerVar));

        method.body().add(headers.invoke("set").arg("Link").arg(newLinkHeader).arg(JExpr.FALSE));

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


    private Map<String, JDefinedClass> generateHandlerClasses(JPackage p, ModelGenerator.ResourceMirror resource) throws JClassAlreadyExistsException {

        Map<String, JDefinedClass> classMap = Maps.newHashMap();

        String handlerName = camel(resource.getName() + "Handler");
        JDefinedClass ifn = p.subPackage("handler")._interface(handlerName);
        ifn.method(JMod.NONE, resource.valueClass, "represent").param(resource.identifierClass, "identifier");

        classMap.put(resource.getName(), ifn);

        for (Operation operation : resource.resource.getOperations().values()) {
            String operationHandlerName = camel(resource.getName()) + camel(operation.getName()) + "Handler";

            ifn = p.subPackage("handler")._interface(operationHandlerName);

            JMethod operationMethod = ifn.method(JMod.NONE, resource.valueClass, "handle" + camel(operation.getName()));
            operationMethod.param(JMod.FINAL, resource.identifierClass, "identifier");
            operationMethod.param(JMod.FINAL, lookupOperationClass(operation), operation.getName());

            classMap.put(operation.getName(), ifn);
        }

        for (ModelGenerator.ViewMirror view : resource.viewClasses.values()) {
            String name = camel(view.view.getMediaTypeName()) + "Handler";

            ifn = p.subPackage("handler")._interface(name);

            ifn.method(JMod.NONE, view.viewClass, "represent").param(resource.identifierClass, "identifier");
            classMap.put(view.view.getMediaTypeName(), ifn);

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

    private JConditional makeIfBlockForMediaTypeElement(JBlock block, JVar representation, MediaTypeElement element) {
        return block._if(resolveRepresentationMediaType(representation)
                .invoke("equals").arg(mediaTypes.getMediaType(element)));
    }

    private JInvocation resolveRepresentationMediaType(JVar representation) {
        return representation.invoke("getMediaType");
    }

}
