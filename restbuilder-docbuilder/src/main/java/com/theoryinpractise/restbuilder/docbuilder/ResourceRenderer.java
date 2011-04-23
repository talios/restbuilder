package com.theoryinpractise.restbuilder.docbuilder;

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

public class ResourceRenderer {

    public Renderable renderResource(final Model model, final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.p().write(resource.getPreamble())._p();

                renderResourceContentType(c, model, resource);
                renderResourceAttributesTable(c, resource);
                renderResourceOperationsDefinitionList(c, resource);

                c.p().write(resource.getComment())._p();

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
        for (Field field : resource.getFields()) {
            c.write("  \"" + field.getName() + "\": \"xxx\",\n");
        }
        c.write("}\n")._pre();
    }


}
