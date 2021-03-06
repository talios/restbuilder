package com.theoryinpractise.restbuilder.parser;

import com.google.common.collect.ImmutableList;
import com.theoryinpractise.restbuilder.parser.model.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class RestBuilderTest {

    private Model model;

    @BeforeMethod
    public void init() throws IOException {
        RestBuilder restBuilder = new RestBuilder();
        restBuilder.setTracingEnabled(true);

        model = restBuilder.buildModel(ImmutableList.<NamedInputSupplier>of(
                new NamedInputSupplier("/account.rbuilder", getResource(RestBuilderTest.class, "/account.rbuilder"))));

    }

    @Test
    public void testBuildModel() throws Exception {

        assertThat(model)
                .describedAs("A restbuilder model object")
                .isNotNull()
                .isInstanceOf(Model.class);

        assertThat(model.getNamespace()).isEqualTo("example");
        assertThat(model.getOperations()).isNotEmpty().hasSize(2);

        Resource accountResource = model.getResources().get("account");
        assertThat(accountResource.getPreamble()).isNotEmpty();
        assertThat(accountResource.getComment()).isNotEmpty();

        Operation cancellationOperation = accountResource.getOperations().get("cancellation");
        assertThat(cancellationOperation.getAttributes()).isNotEmpty();

    }


    @Test
    public void testModelAttributeContainsSlashedComments() {

        Operation operation = model.getOperations().get("cancellation");
        if (operation != null) {

            assertThat(operation.getPreamble()).contains("Request the cancellation of a given resource.");

            OperationAttribute attribute = operation.getAttributes().iterator().next();

            assertThat(attribute.getName()).isEqualTo("thruDate");
            assertThat(attribute.getComment()).isEqualTo("The requested cancellation date.");

            return;
        }

        fail("No operation found for cancellation");
    }

    @Test
    public void testModelIdentifierContainsSlashedComments() {

        Resource resource = model.getResources().get("account");
        if (resource != null) {

            Identifier id = resource.getIdentifiers().iterator().next();

            assertThat(id.getName()).isEqualTo("id");
            assertThat(id.getComment()).isEqualTo("The key field");

            return;
        }

        fail("No resoruce found for account");
    }

}
