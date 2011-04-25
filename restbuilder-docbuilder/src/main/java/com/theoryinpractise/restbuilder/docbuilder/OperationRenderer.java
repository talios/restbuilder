package com.theoryinpractise.restbuilder.docbuilder;

import com.google.common.collect.Lists;
import com.theoryinpractise.restbuilder.parser.model.*;
import org.pegdown.PegDownProcessor;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import java.io.IOException;
import java.util.List;

import static com.theoryinpractise.restbuilder.codegen.api.MediaTypeBuilder.buildContentType;
import static org.rendersnake.HtmlAttributesFactory.href;

public class OperationRenderer extends AbstractRenderer {

    private PegDownProcessor pegdown = new PegDownProcessor();

    public Renderable renderOperation(final Model model, final Operation operation) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                c.div().write(pegdown.markdownToHtml(operation.getPreamble()), false)._div();

                renderAttributesTable(c,
                        "Content-Type", buildContentType(model, operation),
                        "Attributes", renderAttributes(operation),
                        "Resources", renderResources(operation, model),
                        "Sample Document", renderSampleResourceDocument(operation)
                );

                c.h2().write("Overview")._h2();
                c.div().write(pegdown.markdownToHtml(operation.getComment()), false)._div();

            }
        };
    }


    private Renderable renderAttributes(final Operation operation) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {
                for (Field field : operation.getAttributes()) {
                    c.span().write(field.getName())._span().br();
                }
            }
        };
    }

    private Renderable renderResources(final Operation operation, final Model model) {
        return new Renderable() {
            @Override
            public void renderOn(HtmlCanvas c) throws IOException {

                List<Resource> resources = Lists.newArrayList();
                for (Resource resource : model.getResources().values()) {
                    for (Operation op : resource.getOperations()) {
                        if (op instanceof OperationDefinition) {
                            if (op.equals(operation)) {
                                resources.add(resource);
                            }
                        }
                        if (op instanceof OperationReference) {
                            if (((OperationReference) op).getRestOperationDefinition().equals(operation))
                                resources.add(resource);
                        }
                    }
                }

                if (!resources.isEmpty()) {
                    for (Resource resource : resources) {
                        c.a(href(resource.getName() + ".html")).write(resource.getName())._a().br();
                    }
                } else {
                    c.em().write("Not used.")._em();
                }
            }
        };
    }


}
