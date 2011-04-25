package com.theoryinpractise.restbuilder.codegen.httpserver;

import com.sun.codemodel.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.theoryinpractise.restbuilder.codegen.api.CodeGenerator;
import com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder;
import com.theoryinpractise.restbuilder.codegen.restlet.AbstractGenerator;
import com.theoryinpractise.restbuilder.parser.model.Identifier;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.*;

/**
 * http://www.java2s.com/Code/Java/JDK-6/LightweightHTTPServer.htm
 */
public class HttpHandlerGenerator extends AbstractGenerator implements CodeGenerator {

    @Override
    public void generate(JCodeModel codeModel, Model model) throws JClassAlreadyExistsException {

        JClass stringRef = codeModel.ref(String.class);

        JPackage p = codeModel._package(model.getPackage());

        String namespaceName = camel(camel(model.getNamespace()) + "Server");
        JDefinedClass c = p.subPackage("httpserver")._class(namespaceName);

        c.javadoc().add("Top level Server");

        JMethod mainMethod = c.method(PUBLIC | STATIC, codeModel.VOID, "main");
        mainMethod.param(FINAL, stringRef.array(), "args");
        mainMethod._throws(IOException.class);

        JVar inetSocketAddress = mainMethod.body().decl(codeModel.ref(InetSocketAddress.class), "addr");
        inetSocketAddress.init(_new(codeModel.ref(InetSocketAddress.class)).arg(lit(8182)));

        JVar server = mainMethod.body().decl(codeModel.ref(HttpServer.class), "server",
                codeModel.ref(HttpServer.class).staticInvoke("create").arg(inetSocketAddress).arg(lit(0)));


        for (Resource resource : model.getResources().values()) {

            // TODO Copied from restlet - extract
            // URI constant
            StringBuilder sb = new StringBuilder("/" + resource.getName());
            if (!resource.getIdentifiers().isEmpty()) {
                for (Identifier identifier : resource.getIdentifiers()) {
                    sb.append("/{").append(identifier.getName()).append("}");
                }
            }
            String resourceUriPath = sb.toString();
            JFieldVar uriRef = c.field(JMod.PUBLIC | JMod.FINAL | JMod.STATIC, stringRef,
                    resource.getName().toUpperCase() + "_URI", JExpr.lit(resourceUriPath));


            JDefinedClass resourceHandlerClass = generateResourceHandler(codeModel, model, c, resource);

            mainMethod.body().add(server.invoke("createContext").arg(uriRef).arg(_new(resourceHandlerClass)));

        }


        mainMethod.body().add(server.invoke("setExecutor").arg(codeModel.ref(Executors.class).staticInvoke("newCachedThreadPool")));
        mainMethod.body().add(server.invoke("start"));

    }

    private JDefinedClass generateResourceHandler(JCodeModel codeModel, Model model, JDefinedClass topClass, Resource resource) throws JClassAlreadyExistsException {

        JClass stringRef = codeModel.ref(String.class);

        String resourceName = camel(camel(resource.getName()) + "Handler");
        JDefinedClass c = topClass._class(JMod.PRIVATE | JMod.STATIC, resourceName);
        c._implements(HttpHandler.class);

        c.javadoc().add("Handler for " + resource.getName() + " operation");

        JMethod handleMethod = c.method(PUBLIC, codeModel.VOID, "handle");
        JVar exchange = handleMethod.param(FINAL, codeModel.ref(HttpExchange.class), "exchange");
        JVar methodRef = handleMethod.body().decl(FINAL, stringRef, "method", exchange.invoke("getRequestMethod"));
        JVar contentTypeRef = handleMethod.body().decl(FINAL, stringRef, "contentType",
                exchange.invoke("getRequestHeaders").invoke("get").arg(lit("Content-Type")).invoke("iterator").invoke("next"));


        JBlock postBlock = handleMethod.body()._if(methodRef.eq(lit("POST")))._then();
        for (Operation operation : resource.getOperations().values()) {
            JBlock opBlock = postBlock._if(contentTypeRef.eq(lit(MediaTypeBuilder.buildContentType(model, operation))))._then();
            opBlock.block().directStatement("// handle operation " + operation.getName());
        }


        return c;

    }

}
