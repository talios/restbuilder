package com.theoryinpractise.restbuilder.parser;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.theoryinpractise.restbuilder.parser.model.*;
import org.parboiled.Rule;
import org.parboiled.support.Var;

import java.util.List;

public class RestBuilderParser extends BaseLanguageParser {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    Rule Expression() {

        Var<String> aPackage = new Var<String>();
        Var<String> aNamespace = new Var<String>();

        return
                Sequence(
                        String("package"),
                        Whitespace(),
                        CodeIdentifier(),
                        aPackage.set(match()),
                        Ch(';'),
                        Optional(Whitespace()),
                        String("namespace"),
                        Whitespace(),
                        CodeIdentifier(),
                        aNamespace.set(match()),
                        Ch(';'),
                        Optional(Whitespace()),
                        ZeroOrMore(FirstOf(Operation(ElementType.MODEL), Resource())),
                        Optional(Whitespace()),
                        EOI,
                        push(makeRestModel(aPackage.get(), aNamespace.get()))
                );

    }

    Rule Resource() {
        Var<String> resourceName = new Var<String>();

        return Sequence(
                Optional(CommentBlock(ElementType.RESOURCE)),
                Optional(Whitespace()),
                String("resource"),
                Whitespace(),
                CodeIdentifier(),
                resourceName.set(match()),
                Block(ZeroOrMore(FirstOf(
                        Whitespace(),
                        Identifier(ElementType.FIELD),
                        Attribute(ElementType.RESOURCE),
                        View(ElementType.VIEW, resourceName),
                        OperationReference(),
                        Operation(ElementType.OPERATION)))),

                push(makeRestResource(resourceName.get()))
        );
    }

    Rule OperationReference() {
        Var<String> operationName = new Var<String>();
        return Sequence(
                String("operation"),
                Whitespace(),
                CodeIdentifier(),
                operationName.set(match()),
                Ch(';'),
                Optional(Whitespace()),
                push(makeOperationReference(operationName.get()))
        );
    }

    OperationReference makeOperationReference(String operationName) {
        return new OperationReference(getContext(), name, operationName);
    }

    Rule Operation(ElementType elementType) {
        Var<String> comment = new Var<String>();
        Var<String> operationName = new Var<String>();

        return Sequence(
                Optional(CommentBlock(ElementType.OPERATION), comment.set(popCommentLines(ElementType.OPERATION))),
                Optional(Whitespace()),
                String("operation"),
                Whitespace(),
                CodeIdentifier(),
                operationName.set(match()),
                Block(ZeroOrMore(Attribute(ElementType.OPERATION))),
                push(makeRestOperationDefinition(elementType, operationName.get(), comment.get()))
        );
    }

    Model makeRestModel(String packageName, String namespace) {

        List<Object> children = popChildValues(OperationDefinition.class, Resource.class);

        return new SimpleModel(packageName, namespace, children);
    }

    Resource makeRestResource(String resourceName) {
        List children = popChildValues(View.class, Identifier.class, ResourceAttribute.class, OperationDefinition.class, OperationReference.class);

        return new Resource(
                getContext(),
                name,
                popCommentLines(ElementType.RESOURCE),
                resourceName,
                children);
    }

    OperationDefinition makeRestOperationDefinition(ElementType elementType, String operationName, String comment) {
        List<OperationAttribute> attributes = popValuesIntoList(elementType, OperationAttribute.class);
        return new OperationDefinition(getContext().getLevel(), comment, operationName, attributes);
    }

    List<Object> popChildValues(Class... aClass) {
        ImmutableSet<Class> matchingClasses = ImmutableSet.copyOf(aClass);

        List values = Lists.newArrayList();
        while (!getContext().getValueStack().isEmpty()) {
            Object o = peek();
            if (matchingClasses.contains(o.getClass())) {
                if (o instanceof Level && ((Level) o).getLevel() > getContext().getLevel()) {
                    pop();
                    values.add(o);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return values;
    }


    Rule Identifier(ElementType elementType) {
        Var<String> attributeName = new Var<String>();
        Var<String> attributeType = new Var<String>();

        return Sequence(
                Optional(CommentBlock(elementType)),
                Optional(Whitespace()),
                String("identifier"),
                Whitespace(),
                Type(),
                attributeType.set(match()),
                Whitespace(),
                CodeIdentifier(),
                attributeName.set(match()),
                Ch(';'),
                Optional(Whitespace()),
                Optional(SlashCommentLine(elementType)),

                push(makeIdentifier(elementType, attributeName.get(), attributeType.get()))
        );
    }

    Rule Attribute(ElementType elementType) {
        Var<String> attributeName = new Var<String>();
        Var<String> attributeType = new Var<String>();

        return Sequence(
                Optional(CommentBlock(elementType)),
                Optional(Whitespace()),
                String("attribute"),
                Whitespace(),
                Type(),
                attributeType.set(match()),
                Whitespace(),
                CodeIdentifier(),
                attributeName.set(match()),
                Ch(';'),
                Optional(Whitespace()),
                Optional(SlashCommentLine(elementType)),
                push(makeAttribute(elementType, attributeName.get(), attributeType.get()))
        );
    }

    Rule View(ElementType elementType, Var<String> resourceNameVar) {
        Var<String> viewName = new Var<String>();

        return Sequence(
                Optional(CommentBlock(elementType)),
                Optional(Whitespace()),
                String("view"),
                Whitespace(),
                CodeIdentifier(),
                viewName.set(match()),
                Block(OneOrMore(FirstOf(
                        Whitespace(),
                        Attribute(ElementType.VIEW)))),

                push(makeView(elementType, resourceNameVar.get(), viewName.get() )
        ));
    }

    Identifier makeIdentifier(ElementType elementType, String name, String type) {
        String comment = popCommentLines(elementType);
        return new Identifier(getContext().getLevel(), comment, name, type);
    }

    Field makeAttribute(ElementType elementType, String name, String type) {
        String comment = popCommentLines(elementType);

        switch (elementType) {
            case RESOURCE: return  new ResourceAttribute(getContext().getLevel(), comment, name, type);
            case OPERATION: return  new OperationAttribute(getContext().getLevel(), comment, name, type);
            case VIEW: return  new ViewAttribute(getContext().getLevel(), comment, name, type);
            default: throw new IllegalArgumentException("Unknown elementType " + elementType.name());
        }

    }

    View makeView(ElementType elementType, String resourceName, String name) {
        List children = popChildValues(ViewAttribute.class);

        String comment = popCommentLines(elementType);
        return  new View(getContext().getLevel(), comment, resourceName, name, children);
    }


}
