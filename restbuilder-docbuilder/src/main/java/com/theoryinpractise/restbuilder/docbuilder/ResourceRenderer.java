package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.collect.Lists;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import com.theoryinpractise.restbuilder.parser.model.View;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;
import java.util.List;

public class ResourceRenderer extends AbstractClassRenderer {

    public Renderable renderResource(final Model model, final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.div().write(pegdown.markdownToHtml(resource.getPreamble()), false)._div();


                List<Object> tableVales = Lists.newArrayList();
                tableVales.add("Operations");
                tableVales.add(renderOperations(resource));

                tableVales.add(renderContentType(model, resource));
                tableVales.add(renderSampleDocument(resource));

                for (View view : resource.getViews().values()) {
                    tableVales.add(renderContentType(model, view));
                    tableVales.add(renderSampleDocument(view));
                }

                renderAttributesTable(c, tableVales.toArray());

                c.h2().write("Overview")._h2();
                c.div().write(pegdown.markdownToHtml(resource.getComment()), false)._div();

            }
        };
    }

    private Renderable renderViews(final Model model, final Resource resource) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.render(renderContentType(model, resource)).br();
                c.render(renderSampleDocument(resource)).br();


                for (View view : resource.getViews().values()) {
                    c.render(renderContentType(model, view)).br();
                    c.render(renderSampleDocument(view)).br();

                }
            }
        };
    }

}
