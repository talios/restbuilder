package com.theoryinpractise.restbuilder.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.theoryinpractise.restbuilder.parser.model.Comment;
import com.theoryinpractise.restbuilder.parser.model.ElementType;
import com.theoryinpractise.restbuilder.parser.model.Level;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;

import java.util.List;

public class BaseLanguageParser extends BaseParser {

    List<Object> popChildValues(Class... aClass) {

        ImmutableList.Builder<Object> valueBuilder = ImmutableList.builder();
        for (Class aClas : aClass) {
            valueBuilder.addAll(popValuesIntoList(aClas));
        }

        return valueBuilder.build();

    }

    protected <T> List<T> popValuesIntoList(Class<T> aClass) {

        List<T> allAttributes = ImmutableList.copyOf(getContext().getValueStack());
        List<T> attributes = Lists.newArrayList();

        for (Object o : allAttributes) {
            if (o instanceof Level && ((Level) o).getLevel() > getContext().getLevel() && aClass.isAssignableFrom(o.getClass())) {
                attributes.add((T) o);
            }
        }

        getContext().getValueStack().clear();

        for (T remainingAttribute : allAttributes) {
            if (!attributes.contains(remainingAttribute)) {
                getContext().getValueStack().push(remainingAttribute);
            }
        }

        return Lists.newArrayList(attributes);
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
            case RESOURCE_REFERENCE:
                docClass = Comment.ResourceReferenceComment.class;
                break;
            case FIELD:
                docClass = Comment.FieldComment.class;
                break;
            case VIEW:
                docClass = Comment.ViewComment.class;
                break;
        }
        return docClass;
    }

    String popCommentLines(ElementType elementType) {
        List<Comment> commentLines = Lists.reverse(popValuesIntoList(getClassForElementType(elementType)));
        return Joiner.on("\n").join(commentLines);
    }

    Rule Block(Rule rule) {
        return Sequence(
                Whitespace(),
                Ch('{'),
                rule,
                Optional(Whitespace()),
                Ch('}'),
                Optional(Whitespace())
        );
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
        return OneOrMore(FirstOf(Alpha(), AnyOf("/\'\"@{}-,.:*_ \t")));
    }

    Comment newComment(int level, ElementType elementType, String comment) {

        switch (elementType) {
            case RESOURCE:
                return new Comment.ResourceComment(level, comment);
            case RESOURCE_REFERENCE:
                return new Comment.ResourceReferenceComment(level, comment);
            case FIELD:
                return new Comment.FieldComment(level, comment);
            case OPERATION:
                return new Comment.OperationComment(level, comment);
            case VIEW:
                return new Comment.ViewComment(level, comment);
        }

        throw new IllegalArgumentException("Unsupported elementType - " + elementType.name());

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
