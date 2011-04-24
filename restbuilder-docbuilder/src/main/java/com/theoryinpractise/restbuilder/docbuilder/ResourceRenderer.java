package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.theoryinpractise.restbuilder.parser.model.Field;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;
import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;

public class ResourceRenderer extends AbstractRenderer {

    public Renderable renderResource(final Model model, final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.div().write(pegdown.markdownToHtml(resource.getPreamble()), false)._div();

                renderAttributesTable(c,
                        "Content-Type", buildContentType(model, resource),
                        "Attributes", renderAttributes(resource),
                        "Operations", renderOperations(resource),
                        "Sample Document", renderSampleResourceDocument(resource)
                );


                c.h2().write("Overview")._h2();
                c.div().write(pegdown.markdownToHtml(resource.getComment()), false)._div();


            }
        };
    }

    private Renderable renderAttributes(final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                for (Field field : resource.getFields()) {
                    c.span().write(field.getName())._span().br();
                }
            }
        };
    }

    private Renderable renderOperations(final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                for (Operation operation : resource.getOperations()) {
                    String name = operation.getName();
                    String title = pegdown.markdownToHtml(operation.getComment());
                    c.a(href(name + ".html").title(title)).write(name)._a().br();
                }
            }
        };
    }

    protected Renderable renderSampleResourceDocument(final Resource resource) throws IOException {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                c.pre(class_("code")).code().write("{\n");

                String jsonContent = Joiner.on(",\n").join(Iterables.transform(resource.getFields(), new Function<Field, String>() {
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
