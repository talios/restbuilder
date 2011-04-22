package com.theoryinpractise.restbuilder.parser;

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
                        Identifier(),
                        aPackage.set(match()),
                        Ch(';'),
                        Optional(Whitespace()),
                        String("namespace"),
                        Whitespace(),
                        Identifier(),
                        aNamespace.set(match()),
                        Ch(';'),
                        Optional(Whitespace()),
                        ZeroOrMore(FirstOf(Operation(), Resource())),


                        Optional(Whitespace()),
                        EOI,
                        push(makeRestModel(aPackage.get(), aNamespace.get()))
                );

    }

    Rule Resource() {
        Var<String> comment = new Var<String>();
        Var<String> resourceName = new Var<String>();

        return Sequence(
                Optional(Javadoc(), comment.set(getContext().getValueStack().pop().toString())),
                Optional(Whitespace()),
                String("resource"),
                Whitespace(),
                Identifier(),
                resourceName.set(match()),
                Whitespace(),
                Ch('{'),
                ZeroOrMore(FirstOf(Attribute(), Whitespace(), OperationReference(), Operation())),

                Optional(Whitespace()),
                Ch('}'),
                Optional(Whitespace()),
                push(makeRestResource(comment.get(), resourceName.get()))
        );
    }

    Rule OperationReference() {
        Var<String> operationName = new Var<String>();
        return Sequence(
                String("operation"),
                Whitespace(),
                Identifier(),
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
                if (op.getName().equals(operationName) && op.getLevel() < getContext().getLevel()) { // TODO why 3?
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

        return new OperationReference(getContext().getLevel(), restOperationDefinition);
    }

    Rule Operation() {
        Var<String> comment = new Var<String>();
        Var<String> operationName = new Var<String>();
        return Sequence(
                Optional(Javadoc(), comment.set(getContext().getValueStack().pop().toString())),
                Optional(Whitespace()),
                String("operation"),
                Whitespace(),
                Identifier(),
                operationName.set(match()),
                Whitespace(),
                Ch('{'),
                ZeroOrMore(Attribute()),
                Optional(Whitespace()),
                Ch('}'),
                Optional(Whitespace()),
                push(makeRestOperationDefinition(comment.get(), operationName.get()))
        );
    }

    Model makeRestModel(String packageName, String namespace) {

        List<Object> children = popChildValues(OperationDefinition.class, Resource.class);

        return new Model(packageName, namespace, children);
    }

    Resource makeRestResource(String comment, String resourceName) {
        List children = popChildValues(Attribute.class, OperationDefinition.class, OperationReference.class);

        return new Resource(
                getContext().getLevel(),
                comment,
                resourceName,
                children);
    }

    OperationDefinition makeRestOperationDefinition(String comment, String operationName) {
        List<Attribute> attributes = popValuesIntoList(Attribute.class);
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

    private <T> List<T> popValuesIntoList(Class<T> aClass) {
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

    Rule Attribute() {
        Var<String> comment = new Var<String>();
        Var<String> attributeName = new Var<String>();
        Var<String> attributeType = new Var<String>();

        return Sequence(
                Optional(Javadoc(), comment.set(getContext().getValueStack().pop().toString())),
                Optional(Whitespace()),
                String("attribute"),
                Whitespace(),
                Type(),
                attributeType.set(match()),
                Whitespace(),
                Identifier(),
                attributeName.set(match()),
                Ch(';'),
                push(new Attribute(getContext().getLevel(), comment.get(), attributeType.get(), attributeName.get()))
        );
    }

    Rule Javadoc() {
        Var<String> comment = new Var<String>();

        return Sequence(
                Optional(Whitespace()),
                String("/**"),
                Whitespace(),
                Optional(String("*")),
                Optional(Whitespace()),
                OneOrMore(FirstOf(Alpha(), AnyOf("/\'\"\n@,."), Whitespace())),
                comment.set(match()),
                Optional(Whitespace()),
                String("*/"),
                Whitespace(),
                push(comment.get()));
    }

    Rule Identifier() {
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
