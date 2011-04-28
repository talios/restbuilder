package com.theoryinpractise.restbuilder.codegen.base;

import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import com.theoryinpractise.restbuilder.parser.model.Field;
import com.theoryinpractise.restbuilder.parser.model.Operation;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AbstractGenerator {

    public static final String OPERATION = "Operation";

    private static Map<String, JDefinedClass> definedClasses = Maps.newHashMap();

    public static String makeOperationClassName(Operation operation) {
        return camel(operation.getName() + OPERATION);
    }

    protected JDefinedClass lookupOperationClass(Operation operation) {
        return definedClasses.get(makeOperationClassName(operation));
    }

    protected static String camel(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    protected JClass resolveFieldType(JCodeModel jCodeModel, Field attr) {
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

    protected JDefinedClass generateImmutableBean(JCodeModel jCodeModel,
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


}
