package com.theoryinpractise.restbuilder.docbuilder;

import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;

/**
 * Render an index div for the RESTBuilder model.
 */
public class IndexRenderer implements Renderable {

    private Model model;

    public IndexRenderer(Model model) {
        this.model = model;
    }

    @Override
    public void renderOn(HtmlCanvas c) throws IOException {

        c.ul();

        c.li().span(class_("indexheading")).write("Resources")._span().ul();
        for (Resource resource : model.getResources()) {
            c.li().a(href(resource.getName() + ".html")).write(capitalize(resource.getName()))._a()._li();
        }
        c._ul()._li();

        c.li().span(class_("indexheading")).write("Operations")._span().ul();
        for (Operation operation : model.getOperations()) {
            c.li().a(href(operation.getName() + ".html")).write(capitalize(operation.getName()))._a()._li();
        }
        c._ul()._li();


        c._ul();


    }
}
