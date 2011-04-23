package com.theoryinpractise.restbuilder.docbuilder;

import com.theoryinpractise.restbuilder.parser.model.Attribute;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;

public class OperationRenderer {

    public Renderable renderOperation(final Model model, final Operation operation) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.p().write(operation.getPreamble())._p();

                c.h3().write("Content-Type")._h3();
                c.write(buildContentType(model, operation));

                c.h3().write("Sample document")._h3();
                c.pre().write("{\n");
                for (Attribute attribute : operation.getAttributes()) {
                    c.write("  \"" + attribute.getName() + "\": \"xxx\",\n");
                }
                c.write("}\n")._pre();

                c.p().write(operation.getComment())._p();

            }
        };
    }

}
