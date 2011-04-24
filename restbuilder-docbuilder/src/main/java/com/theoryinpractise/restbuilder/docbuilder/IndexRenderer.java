package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.theoryinpractise.restbuilder.parser.model.*;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

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


        List<Operation> operations = Lists.newArrayList();
        for (Operation operation : model.getOperations()) {
            operations.add(operation);
        }
        for (Resource resource : model.getResources()) {
            for (Operation operation : resource.getOperations()) {
                if (operation instanceof OperationDefinition) {
                    operations.add(operation);
                }
            }
        }

        List<Operation> sortedOperations = Ordering.from(new Comparator<Operation>() {
            @Override
            public int compare(Operation operation, Operation operation1) {
                return ComparisonChain.start()
                        .compare(operation.getName(), operation1.getName())
                        .result();
            }
        }).sortedCopy(operations);

        c.li().span(class_("indexheading")).write("Operations")._span().ul();
        for (Operation operation : sortedOperations) {
            c.li().a(href(operation.getName() + ".html")).write(capitalize(operation.getName()))._a()._li();
        }
        c._ul()._li();


        c._ul();


    }
}
