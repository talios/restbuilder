package com.theoryinpractise.restbuilder.parser;

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

        Var<String> aNamespace = new Var<String>();

        return Sequence(
                Optional(Whitespace()),
                String("namespace"),
                Whitespace(),
                CodeIdentifier(),
                aNamespace.set(match()),
                Block(ZeroOrMore(FirstOf(Operation(ElementType.MODEL), Resource()))),
                EOI,
                push(makeRestModel(aNamespace.get())));

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
                        Operation(ElementType.OPERATION),
                        ResourceCollectionReference()
                        ))),

                push(makeRestResource(resourceName.get()))
        );
    }

    Rule ResourceCollectionReference() {
        Var<String> resourceName = new Var<String>();
        Var<Boolean> unary = new Var<Boolean>(Boolean.FALSE);

        return Sequence(
                Optional(CommentBlock(ElementType.RESOURCE_REFERENCE)),
                Optional(Whitespace()),
                String("resource"),
                Optional(String(" collection"), unary.set(Boolean.TRUE)),
                Whitespace(),
                CodeIdentifier(),
                resourceName.set(match()),
                Block(ZeroOrMore(FirstOf(
                        Whitespace(),
                        View(ElementType.VIEW, resourceName),
                        OperationReference(),
                        Operation(ElementType.OPERATION)
                ))),
                push(makeRestResourceReference(resourceName.get(), unary.get()))
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

    Model makeRestModel(String namespace) {

        List<Object> children = popChildValues(OperationDefinition.class, Resource.class);

        return new SimpleModel(namespace, children);
    }

    Resource makeRestResource(String resourceName) {
        List children = popChildValues(View.class, Identifier.class, ResourceAttribute.class, OperationDefinition.class, OperationReference.class);

        return new Resource(getContext(), name, popCommentLines(ElementType.RESOURCE), resourceName, children);
    }

    ResourceReference makeRestResourceReference(String resourceName, boolean unary) {
        List children = popChildValues(View.class, OperationDefinition.class, OperationReference.class);
        return new ResourceReference(getContext(), name, unary, popCommentLines(ElementType.RESOURCE_REFERENCE), resourceName, children);
    }

    OperationDefinition makeRestOperationDefinition(ElementType elementType, String operationName, String comment) {
        List<OperationAttribute> attributes = popValuesIntoList(OperationAttribute.class);
        return new OperationDefinition(getContext().getLevel(), comment, operationName, attributes);
    }

    Rule Identifier(ElementType elementType) {
        Var<String> attributeName = new Var<String>();
        Var<String> attributeType = new Var<String>();

        return Sequence(
                Optional(CommentBlock(ElementType.FIELD)),
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
                Optional(SlashCommentLine(ElementType.FIELD)),

                push(makeIdentifier(elementType, attributeName.get(), attributeType.get()))
        );
    }

    Rule Attribute(ElementType elementType) {
        Var<String> attributeName = new Var<String>();
        Var<String> attributeType = new Var<String>();

        return Sequence(
                Optional(CommentBlock(ElementType.FIELD)),
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
                Optional(SlashCommentLine(ElementType.FIELD)),
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

                push(makeView(elementType, resourceNameVar.get(), viewName.get())
                ));
    }

    Identifier makeIdentifier(ElementType elementType, String name, String type) {
        String comment = popCommentLines(elementType);
        return new Identifier(getContext().getLevel(), comment, name, type);
    }

    Field makeAttribute(ElementType elementType, String name, String type) {
        String comment = popCommentLines(ElementType.FIELD);

        switch (elementType) {
            case RESOURCE:
                return new ResourceAttribute(getContext().getLevel(), comment, name, type);
            case RESOURCE_REFERENCE:
                return new ResourceReferenceAttribute(getContext().getLevel(), comment, name, type);
            case OPERATION:
                return new OperationAttribute(getContext().getLevel(), comment, name, type);
            case VIEW:
                return new ViewAttribute(getContext().getLevel(), comment, name, type);
            default:
                throw new IllegalArgumentException("Unknown elementType " + elementType.name());
        }

    }

    View makeView(ElementType elementType, String resourceName, String name) {
        List children = popChildValues(ViewAttribute.class);

        String comment = popCommentLines(elementType);
        return new View(getContext().getLevel(), comment, resourceName, name, children);
    }


}
