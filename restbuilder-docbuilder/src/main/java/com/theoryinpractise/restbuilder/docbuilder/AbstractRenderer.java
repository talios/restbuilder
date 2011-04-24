package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.theoryinpractise.restbuilder.parser.model.Field;
import com.theoryinpractise.restbuilder.parser.model.FieldHolder;
import org.pegdown.PegDownProcessor;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;

import static org.rendersnake.HtmlAttributesFactory.class_;

public class AbstractRenderer {
    protected PegDownProcessor pegdown = new PegDownProcessor();

    protected void renderAttributesTable(HtmlCanvas c, Object... nameValuePairs) throws IOException {

        int i = 0;

        ArrayDeque<Object> deque = new ArrayDeque<Object>(Arrays.asList(nameValuePairs));

        c.table(class_("details")).tbody();

        while (!deque.isEmpty()) {
            c.tr().td(class_("heading"));

            if (deque.peek() instanceof Renderable) {
                c.render((Renderable) deque.poll());
            } else {
                c.write(deque.poll().toString());
            }

            c._td().td(class_("value"));

            if (deque.peek() instanceof Renderable) {
                c.render((Renderable) deque.poll());
            } else {
                c.write(pegdown.markdownToHtml(deque.poll().toString()));
            }

            c._td()._tr();
        }

        c._tbody()._table();

    }

    protected Renderable renderSampleResourceDocument(final FieldHolder holder) throws IOException {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                c.pre(class_("code")).code().write("{\n");

                String jsonContent = Joiner.on(",\n").join(Iterables.transform(holder.getFields(), new Function<Field, String>() {
                    @Override
                    public String apply(Field field) {
                        return "  \"" + field.getName() + "\": \"xxx\"";
                    }
                }));

                c.write(jsonContent);
                c.write("\n}\n")._code()._pre();

            }
        };
    }


}
