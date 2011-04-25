package com.theoryinpractise.restbuilder.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.theoryinpractise.restbuilder.parser.model.*;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;

import java.util.List;

public class RestBuilderParser extends BaseParser {

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
                Whitespace(),
                Ch('{'),
                ZeroOrMore(FirstOf(
                        Whitespace(),
                        Identifier(ElementType.FIELD),
                        Attribute(ElementType.FIELD),
                        OperationReference(),
                        Operation(ElementType.OPERATION))),

                Optional(Whitespace()),
                Ch('}'),
                Optional(Whitespace()),
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

        OperationDefinition restOperationDefinition = null;
        for (Object o : getContext().getValueStack()) {
            if (o instanceof OperationDefinition) {
                OperationDefinition op = (OperationDefinition) o;
                if (op.getName().equals(operationName) && op.getLevel() < getContext().getLevel()) {
                    restOperationDefinition = op;
                    break;
                }
            }
        }

        if (restOperationDefinition == null) {
            throw new IllegalStateException(String.format(
                    "operation reference to unknown operation '%s' on line %s",
                    operationName,
                    getContext().getInputBuffer().getPosition(getContext().getMatchStartIndex()).line));
        }

        return new OperationReference(getContext().getLevel(), ElementType.OPERATION, restOperationDefinition);
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
                Whitespace(),
                Ch('{'),
                ZeroOrMore(Attribute(ElementType.OPERATION)),
                Optional(Whitespace()),
                Ch('}'),
                Optional(Whitespace()),
                push(makeRestOperationDefinition(elementType, operationName.get(), comment.get()))
        );
    }

    Model makeRestModel(String packageName, String namespace) {

        List<Object> children = popChildValues(ElementType.MODEL, OperationDefinition.class, Resource.class);

        return new Model(packageName, namespace, children);
    }

    Resource makeRestResource(String resourceName) {
        List children = popChildValues(ElementType.RESOURCE, Identifier.class, Attribute.class, OperationDefinition.class, OperationReference.class);

        return new Resource(
                getContext().getLevel(),
                popCommentLines(ElementType.RESOURCE),
                resourceName,
                children);
    }

    OperationDefinition makeRestOperationDefinition(ElementType elementType, String operationName, String comment) {
        List<Attribute> attributes = popValuesIntoList(elementType, Attribute.class);
        return new OperationDefinition(getContext().getLevel(), elementType, comment, operationName, attributes);
    }

    List<Object> popChildValues(ElementType parentType, Class... aClass) {
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


    private <T> List<T> popValuesIntoList(ElementType parentType, Class<T> aClass) {
        List<T> attributes = Lists.newArrayList();
        while (!getContext().getValueStack().isEmpty()) {
            Object o = peek();
            if (o instanceof Level && ((Level) o).getLevel() > getContext().getLevel() && aClass.isAssignableFrom(o.getClass())) {
                pop();
                attributes.add((T) o);
            } else {
                break;
            }
        }
        return attributes;
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

    Identifier makeIdentifier(ElementType elementType, String name, String type) {
        String comment = popCommentLines(elementType);
        return new Identifier(getContext().getLevel(), elementType, comment, name, type);
    }

    Attribute makeAttribute(ElementType elementType, String name, String type) {
        String comment = popCommentLines(elementType);
        return  new Attribute(getContext().getLevel(), elementType, comment, name, type);
    }

    Class getClassForElementType(ElementType elementType) {
        Class docClass = null;
        switch (elementType) {
            case OPERATION:
                docClass = Comment.OperationComment.class;
                break;
            case RESOURCE:
                docClass = Comment.ResourceComment.class;
                break;
            case FIELD:
                docClass = Comment.FieldComment.class;
                break;
        }
        return docClass;
    }

    String popCommentLines(ElementType elementType) {
        List<Comment> commentLines = Lists.reverse(popValuesIntoList(ElementType.OPERATION, getClassForElementType(elementType)));
        return Joiner.on("\n").join(commentLines);
    }


    Rule CommentBlock(ElementType elementType) {
        return OneOrMore(FirstOf(
                MultilineAsteriskCommentBlock(elementType),
                MultilineSlashCommentBlock(elementType)));
    }

    Rule MultilineAsteriskCommentBlock(ElementType elementType) {

        return Sequence(
                Optional(Whitespace()),
                String("/**\n"),
                OneOrMore(FirstOf(EmptyAsteriskCommentLine(elementType), AsteriskCommentLine(elementType))),
                Sequence(Whitespace(), String("*/\n")));
    }

    Rule AsteriskCommentLine(ElementType elementType) {
        Var<String> comment = new Var<String>();

        return Sequence(
                Whitespace(),
                String("* "),
                CommentContent(),
                comment.set(match()),
                Ch('\n'),
                push(newComment(getContext().getLevel(), elementType, comment.get()))

        );
    }

    Rule EmptyAsteriskCommentLine(ElementType elementType) {

        return Sequence(
                Whitespace(),
                String("*\n"),
                push(newComment(getContext().getLevel(), elementType, ""))

        );
    }

    Rule MultilineSlashCommentBlock(ElementType elementType) {

        return Sequence(
                Optional(Whitespace()),
                OneOrMore(FirstOf(EmptySlashCommentLine(elementType), SlashCommentLine(elementType))));
    }

    Rule SlashCommentLine(ElementType elementType) {
        Var<String> comment = new Var<String>();

        return Sequence(
                Optional(Whitespace()),
                String("// "),
                CommentContent(),
                comment.set(match()),
                Ch('\n'),
                push(newComment(getContext().getLevel(), elementType, comment.get()))

        );
    }

    Rule EmptySlashCommentLine(ElementType elementType) {

        return Sequence(
                Whitespace(),
                String("//\n"),
                push(newComment(getContext().getLevel(), elementType, ""))

        );
    }

    Rule CommentContent() {
        return OneOrMore(FirstOf(Alpha(), AnyOf("/\'\"@,.:*_ \t")));
    }

    Comment newComment(int level, ElementType elementType, String comment) {

        switch (elementType) {
            case RESOURCE: return new Comment.ResourceComment(level, elementType, comment);
            case FIELD: return new Comment.FieldComment(level, elementType, comment);
            case OPERATION: return new Comment.OperationComment(level, elementType, comment);
        }

        throw new IllegalArgumentException("Unsupport elementType - " + elementType.name());

    }

    Rule CodeIdentifier() {
        return OneOrMore(FirstOf(Alpha(), Numeric(), AnyOf(".")));
    }

    Rule Type() {
        return OneOrMore(Alpha());
    }


    Rule Numeric() {
        return CharRange('0', '9');
    }

    Rule Alpha() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    Rule Whitespace() {
        return OneOrMore(AnyOf(" \t\n"));
    }

}
