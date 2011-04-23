package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.theoryinpractise.restbuilder.parser.model.Attribute;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import org.pegdown.PegDownProcessor;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;
import static org.rendersnake.HtmlAttributesFactory.class_;

public class OperationRenderer {

    private PegDownProcessor pegdown = new PegDownProcessor();

    public Renderable renderOperation(final Model model, final Operation operation) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.div().write(pegdown.markdownToHtml(operation.getPreamble()), false)._div();

                renderOperationContentType(c, model, operation);
                renderResourceAttributesTable(c, operation);

                c.p()._p();
                c.div().write(pegdown.markdownToHtml(operation.getComment()), false)._div();

                renderOperationSampleDocument(c, operation);

            }
        };
    }

    private void renderResourceAttributesTable(HtmlCanvas c, Operation operation) throws IOException {
        c.h3().write("Attributes")._h3();
        // attributes
        c.table().thead();
        c.th().write("Name")._th();
        c.th().write("Type")._th();
        c.th().write("Description")._th();
        c._thead();
        c.tbody();

        boolean oddRow = false;
        for (Attribute attribute : operation.getAttributes()) {
            c.tr();
            c.td(class_(oddRow ? "odd" : "even")).write(attribute.getName())._td();
            c.td(class_(oddRow ? "odd" : "even")).write(attribute.getType())._td();
            c.td(class_(oddRow ? "odd" : "even")).write(attribute.getComment())._td();
            c._tr();
            oddRow = !oddRow;
        }
        c._tbody();
        c._table();
    }

    private void renderOperationSampleDocument(HtmlCanvas c, Operation operation) throws IOException {
        c.h3().write("Sample document")._h3();
        c.pre().write("{\n");

        String jsonContent = Joiner.on(",\n").join(Iterables.transform(operation.getAttributes(), new Function<Attribute, String>() {
            @Override
            public String apply(Attribute attribute) {
                return "  \"" + attribute.getName() + "\": \"xxx\"";
            }
        }));

        c.write(jsonContent);
        c.write("\n}\n")._pre();
    }

    private void renderOperationContentType(HtmlCanvas c, Model model, Operation operation) throws IOException {
        c.h3().write("Content-Type")._h3();
        c.write(buildContentType(model, operation));
    }

}
