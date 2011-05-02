package com.theoryinpractise.restbuilder.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.theoryinpractise.restbuilder.parser.model.Comment;
import com.theoryinpractise.restbuilder.parser.model.ElementType;
import com.theoryinpractise.restbuilder.parser.model.Level;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;

import java.util.List;

public class BaseLanguageParser extends BaseParser {

    protected <T> List<T> popValuesIntoList(ElementType parentType, Class<T> aClass) {
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
            case VIEW:
                docClass = Comment.ViewComment.class;
                break;
        }
        return docClass;
    }

    String popCommentLines(ElementType elementType) {
        List<Comment> commentLines = Lists.reverse(popValuesIntoList(elementType, getClassForElementType(elementType)));
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
        return OneOrMore(FirstOf(Alpha(), AnyOf("/\'\"@,.:*_ \t")));
    }

    Comment newComment(int level, ElementType elementType, String comment) {

        switch (elementType) {
            case RESOURCE:
                return new Comment.ResourceComment(level, comment);
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
