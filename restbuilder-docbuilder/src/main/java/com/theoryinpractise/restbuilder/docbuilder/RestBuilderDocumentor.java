package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.theoryinpractise.restbuilder.parser.model.*;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.rendersnake.HtmlAttributesFactory.*;

public class RestBuilderDocumentor {
    public void generateDocumentation(File outputBase, Model model) throws IOException {

        outputBase.mkdirs();

//        renderToFile(new File(outputBase, "index.html"), model.getNamespace() + " REST Documentation", renderResourceList(model));

        for (Operation operation : model.getOperations()) {
            renderToFile(model, new File(outputBase, operation.getName() + ".html"), capitalize(operation.getName()), renderOperation(model, operation));
        }

        for (Resource resource : model.getResources()) {
            renderToFile(model, new File(outputBase, resource.getName() + ".html"), capitalize(resource.getName()), renderResource(model, resource));
        }

    }

    private Renderable renderResource(final Model model, final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.p().write(resource.getPreamble())._p();

                c.h3().write("Content-Type")._h3();
                c.p().write(buildContentType(model, resource))._p();

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


                c.h3().write("Sample document")._h3();
                c.pre(class_("sample")).write("{\n");
                for (Attribute attribute : resource.getAttributes()) {
                    c.write("  \"" + attribute.getName() + "\": \"xxx\",\n");
                }
                c.write("}\n")._pre();


                c.h3().write("Operations")._h3();

                c.dl();
                for (Operation restOperation : model.getOperations()) {
                    c.dt().a(href(restOperation.getName() + ".html")).write(capitalize(restOperation.getName()))._a()._dt();
                    c.dd().write(restOperation.getPreamble())._dd();
                }
                c._dl();


                c.p().write(resource.getComment())._p();


            }
        };
    }

    private Renderable renderOperation(final Model model, final Operation operation) {
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


    private Renderable renderResourceList(final Model model) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                c.h2().write("Media Types")._h2();
                c.ul();
                for (Resource restResource : model.getResources()) {
                    c.li().a(href(restResource.getName() + ".html")).write(buildContentType(model, restResource))._a()._li();
                }
                c._ul();
                c.h2().write("Operations")._h2();
                c.ul();
                for (Operation restOperation : model.getOperations()) {
                    c.li().a(href(restOperation.getName() + ".html")).write(restOperation.getName())._a()._li();
                }
                c._ul();

            }
        };
    }


    private void renderToFile(Model model, File file, String title, Renderable renderable) throws IOException {

        InputSupplier<InputStreamReader> supplier = Resources.newReaderSupplier(
                RestBuilderDocumentor.class.getResource("/documentation.css"), Charsets.UTF_8);

        // <link href='http://fonts.googleapis.com/css?family=Cantarell' rel='stylesheet' type='text/css'>


        HtmlCanvas html = new HtmlCanvas();
        html.html()
                .head()
                .link(href("http://fonts.googleapis.com/css?family=Cantarell").rel("stylesheet").type("text/css"))
                .style().write(CharStreams.toString(supplier))._style()
                ._head();
        html
                .body()

                .div(id("index"))
                .render(new IndexRenderer(model))
                ._div()

                .div(id("header"))
                .h1().write(title)._h1()
                ._div()

                .div(id("content"))
                .render(renderable)
                ._div()

                .div(id("footer"))
                .write("Documentation build at " + new Date().toString())
                ._div()

                ._body()
                ._html();

        renderToFie(html, file);
    }


    private void renderToFie(HtmlCanvas html, File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(html.toHtml());
        fw.close();
    }
}
