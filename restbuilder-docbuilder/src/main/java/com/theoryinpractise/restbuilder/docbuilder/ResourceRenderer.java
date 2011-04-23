package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.theoryinpractise.restbuilder.parser.model.*;
import org.pegdown.PegDownProcessor;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;

public class ResourceRenderer {

    private PegDownProcessor pegdown = new PegDownProcessor();

    public Renderable renderResource(final Model model, final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.div().write(pegdown.markdownToHtml(resource.getPreamble()), false)._div();

                renderResourceContentType(c, model, resource);
                renderResourceAttributesTable(c, resource);
                renderResourceOperationsDefinitionList(c, resource);

                c.div().write(pegdown.markdownToHtml(resource.getComment()), false)._div();

                renderSampleResourceDocument(c, resource);

            }
        };
    }

    private void renderResourceContentType(HtmlCanvas c, Model model, Resource resource) throws IOException {
        c.h3().write("Content-Type")._h3();
        c.p().write(buildContentType(model, resource))._p();
    }

    private void renderResourceOperationsDefinitionList(HtmlCanvas c, Resource resource) throws IOException {
        c.h3().write("Operations")._h3();
        c.dl();
        for (Operation restOperation : resource.getOperations()) {
            c.dt().a(href(restOperation.getName() + ".html")).write(capitalize(restOperation.getName()))._a()._dt();
            c.dd().write(restOperation.getPreamble())._dd();
        }
        c._dl();
    }

    private void renderResourceAttributesTable(HtmlCanvas c, Resource resource) throws IOException {
        c.h3().write("Attributes")._h3();
        // attributes
        c.table().thead();
        c.th().write("Name")._th();
        c.th().write("Type")._th();
        c.th().write("Description")._th();
        c._thead();
        c.tbody();

        boolean oddRow = false;
        for (Field field : resource.getFields()) {
            c.tr();
            c.td(class_(oddRow ? "odd" : "even")).write(field.getName())._td();
            c.td(class_(oddRow ? "odd" : "even")).write(field.getType())._td();
            c.td(class_(oddRow ? "odd" : "even")).write(field.getComment())._td();
            c._tr();
            oddRow = !oddRow;
        }
        c._tbody();
        c._table();
    }

    private void renderSampleResourceDocument(HtmlCanvas c, Resource resource) throws IOException {
        c.h3().write("Sample document")._h3();
        c.pre(class_("sample")).write("{\n");

        String jsonContent = Joiner.on(",\n").join(Iterables.transform(resource.getFields(), new Function<Field, String>() {
            @Override
            public String apply(Field field) {
                return "  \"" + field.getName() + "\": \"xxx\"";
            }
        }));

        c.write(jsonContent);
        c.write("\n}\n")._pre();

    }


}
