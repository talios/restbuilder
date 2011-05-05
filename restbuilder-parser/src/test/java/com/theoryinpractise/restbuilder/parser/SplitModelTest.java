package com.theoryinpractise.restbuilder.parser;

import com.google.common.collect.ImmutableList;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.MultiModel;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static org.fest.assertions.Assertions.assertThat;

public class SplitModelTest {

    @Test
    public void testSplitModels() throws IOException {

        RestBuilder restBuilder = new RestBuilder();
        restBuilder.setTracingEnabled(true);

        Model combined = restBuilder.buildModel(ImmutableList.<NamedInputSupplier>of(
                new NamedInputSupplier("/splitmodel/account.rbuilder", getResource(RestBuilderTest.class, "/splitmodel/account.rbuilder")),
                new NamedInputSupplier("/spltmodel/operations.rbuilder", getResource(RestBuilderTest.class, "/splitmodel/operations.rbuilder"))));

        assertThat(combined)
                .describedAs("A restbuilder model object")
                .isNotNull()
                .isInstanceOf(MultiModel.class);

        assertThat(combined.getNamespace()).isEqualTo("example");
        assertThat(combined.getOperations()).isNotEmpty().hasSize(2);


        Resource accountResource = combined.getResources().get("account");
        assertThat(accountResource.getPreamble()).isNotEmpty();
        assertThat(accountResource.getComment()).isNotEmpty();

        Operation cancellationOperation = accountResource.getOperations().get("cancellation");

        assertThat(cancellationOperation.getAttributes()).isNotEmpty();

    }


}
