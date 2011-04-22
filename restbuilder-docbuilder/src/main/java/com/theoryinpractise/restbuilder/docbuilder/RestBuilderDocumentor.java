package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.theoryinpractise.restbuilder.parser.model.Attribute;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;
import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;

public class RestBuilderDocumentor {
    public void generateDocumentation(File outputBase, Model model) throws IOException {

        outputBase.mkdirs();

        renderToFile(new File(outputBase, "index.html"), model.getNamespace() + " REST Documentation", renderResourceList(model));

        for (Operation operation : model.getOperations()) {
            renderToFile(new File(outputBase, operation.getName() + ".html"), operation.getName(), renderOperation(model, operation));
        }

        for (Resource resource : model.getResources()) {
            renderToFile(new File(outputBase, resource.getName() + ".html"), resource.getName(), renderResource(model, resource));
        }

    }

    private Renderable renderResource(final Model model, final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                if (resource.getComment() != null) {
                    c.p().write(resource.getComment())._p();
                }

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
                for (Attribute attribute : resource.getAttributes()) {
                    c.tr();
                    c.td(class_(oddRow ? "odd" : "even")).write(attribute.getAttributeName())._td();
                    c.td(class_(oddRow ? "odd" : "even")).write(attribute.getAttributeType())._td();
                    c.td(class_(oddRow ? "odd" : "even")).write(attribute.getComment())._td();
                    c._tr();
                    oddRow = !oddRow;
                }
                c._tbody();
                c._table();


                c.h3().write("Sample document")._h3();
                c.pre().write("{\n");
                for (Attribute attribute : resource.getAttributes()) {
                    c.write("  \"" + attribute.getAttributeName() + "\": \"xxx\",\n");
                }
                c.write("{\n")._pre();


                c.h3().write("Operations")._h3();
                for (Operation restOperation : model.getOperations()) {
//                    final String comment = restOperation.getComment() == null ? "" : restOperation.getComment().substring(0, restOperation.getComment().indexOf(".") + 1 );
//                    c.li().a(href(restOperation.getName() + ".html")).write(restOperation.getName())._a()
//                            .write(" " + comment)
//                            ._li();
                    c.render(renderOperation(model, restOperation));
                }


            }
        };
    }

    private Renderable renderOperation(final Model model, final Operation operation) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                if (operation.getComment() != null) {
                    c.p().em().q().write(operation.getComment())._q()._em()._p();
                }

                c.h3().write("Content-Type")._h3();
                c.write(buildContentType(model, operation));

                c.h3().write("Sample document")._h3();
                c.pre().write("{\n");
                for (Attribute attribute : operation.getAttributes()) {
                    c.write("  \"" + attribute.getAttributeName() + "\": \"xxx\",\n");
                }
                c.write("{\n")._pre();

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


    private void renderToFile(File file, String title, Renderable renderable) throws IOException {

        InputSupplier<InputStreamReader> supplier = Resources.newReaderSupplier(
                RestBuilderDocumentor.class.getResource("/documentation.css"), Charsets.UTF_8);


        HtmlCanvas html = new HtmlCanvas();
        html.html().head().style().write(CharStreams.toString(supplier))._style()._head();
        html
                .body()
                .h1().write(title)._h1()
                .render(renderable)
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
