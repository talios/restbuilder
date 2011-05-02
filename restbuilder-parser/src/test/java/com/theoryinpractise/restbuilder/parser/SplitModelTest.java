package com.theoryinpractise.restbuilder.parser;

import com.google.common.base.Charsets;
import com.google.common.io.InputSupplier;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.MultiModel;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.parboiled.common.ImmutableList;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.newReaderSupplier;
import static org.fest.assertions.Assertions.assertThat;

public class SplitModelTest {

    @Test
    public void testSplitModels() throws IOException {

        RestBuilder restBuilder = new RestBuilder();

        Model combined = restBuilder.buildModel(ImmutableList.<InputSupplier<InputStreamReader>>of(
                newReaderSupplier(getResource(SplitModelTest.class, "/splitmodel/account.rbuilder"), Charsets.UTF_8),
                newReaderSupplier(getResource(SplitModelTest.class, "/splitmodel/operations.rbuilder"), Charsets.UTF_8)));

        assertThat(combined)
                .describedAs("A restbuilder model object")
                .isNotNull()
                .isInstanceOf(MultiModel.class);

        assertThat(combined.getPackage()).isEqualTo("com.example.rbuilder");
        assertThat(combined.getNamespace()).isEqualTo("example");
        assertThat(combined.getOperations()).isNotEmpty().hasSize(2);


        Resource accountResource = combined.getResources().get("account");
        assertThat(accountResource.getPreamble()).isNotEmpty();
        assertThat(accountResource.getComment()).isNotEmpty();

        Operation cancellationOperation = accountResource.getOperations().get("cancellation");

        assertThat(cancellationOperation.getAttributes()).isNotEmpty();

    }


}
