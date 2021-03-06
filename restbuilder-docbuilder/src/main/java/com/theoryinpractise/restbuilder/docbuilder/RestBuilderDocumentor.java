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

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.rendersnake.HtmlAttributesFactory.id;

public class RestBuilderDocumentor {
    public void generateDocumentation(File outputBase, Model model) throws IOException {

        outputBase.mkdirs();

        OperationRenderer operationRenderer = new OperationRenderer();
        ResourceRenderer resourceRenderer = new ResourceRenderer();

        for (Operation operation : model.getOperations().values()) {
            renderOperation(outputBase, model, operationRenderer, operation);
        }

        for (Resource resource : model.getResources().values()) {
            renderResource(outputBase, model, resourceRenderer, resource);

            for (Operation operation : resource.getOperations().values()) {
                if (operation instanceof OperationDefinition) {
                    renderOperation(outputBase, model, operationRenderer, operation);
                }
            }
        }

    }

    private void renderResource(File outputBase, Model model, ResourceRenderer resourceRenderer, Resource resource) throws IOException {
        renderToFile(model,
                new File(outputBase, resource.getName() + ".html"),
                capitalize(resource.getName()),
                resourceRenderer.renderResource(model, resource));

    }

    private void renderOperation(File outputBase, Model model, OperationRenderer operationRenderer, Operation operation) throws IOException {
        renderToFile(model,
                new File(outputBase, operation.getName() + ".html"),
                capitalize(operation.getName()),
                operationRenderer.renderOperation(model, operation));
    }

    private void renderToFile(Model model, File file, String title, Renderable renderable) throws IOException {

        InputSupplier<InputStreamReader> supplier = Resources.newReaderSupplier(
                RestBuilderDocumentor.class.getResource("/documentation.css"), Charsets.UTF_8);

        HtmlCanvas html = new HtmlCanvas();
        html.html()
                .head()
//                .link(href("http://fonts.googleapis.com/css?family=Cantarell").rel("stylesheet").type("text/css"))
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
