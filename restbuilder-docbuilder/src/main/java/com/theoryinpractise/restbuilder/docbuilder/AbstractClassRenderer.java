package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.theoryinpractise.restbuilder.parser.BaseClassElement;
import com.theoryinpractise.restbuilder.parser.model.Field;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;

/**
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 30/04/11
 * Time: 8:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class AbstractClassRenderer extends AbstractRenderer {
    protected Renderable renderContentType(final Model model, final BaseClassElement element) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                c.pre().write(buildContentType(model, element))._pre();
            }
        };
    }

    protected Renderable renderAttributes(final BaseClassElement element) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                for (Field field : element.getFields()) {
                    c.span().write(field.getName())._span().br();
                }
            }
        };
    }

    protected Renderable renderOperations(final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                for (Operation operation : resource.getOperations().values()) {
                    String name = operation.getName();
                    String title = pegdown.markdownToHtml(operation.getComment());
                    c.a(href(name + ".html")).write(capitalize(name))._a()
                            .write("&nbsp;&mdash;&nbsp;", false)
                            .write(title, false)
                            .br();
                }
            }
        };
    }

    protected Renderable renderSampleDocument(final BaseClassElement element) throws IOException {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                c.pre(class_("code")).code().write("{\n");

                String jsonContent = Joiner.on(",\n").join(Iterables.transform(element.getFields(), new Function<Field, String>() {
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
