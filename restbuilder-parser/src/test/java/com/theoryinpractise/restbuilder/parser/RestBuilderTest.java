package com.theoryinpractise.restbuilder.parser;

import com.theoryinpractise.restbuilder.parser.model.Attribute;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class RestBuilderTest {

    private Model model;

    @BeforeMethod
    public void init() throws IOException {
        RestBuilder restBuilder = new RestBuilder();
        restBuilder.setTracingEnabled(true);

         model = restBuilder.buildModel(RestBuilderTest.class.getResource("/account.rbuilder"));

    }

    @Test
    public void testBuildModel() throws Exception {


        assertThat(model)
                .describedAs("A restbuilder model object")
                .isNotNull()
                .isInstanceOf(Model.class);

        assertThat(model.getPackage()).isEqualTo("com.example.rbuilder");
        assertThat(model.getNamespace()).isEqualTo("example");
        assertThat(model.getOperations()).isNotEmpty().hasSize(2);


        Resource accountResource = model.getResources().iterator().next();
        assertThat(accountResource.getPreamble()).isNotEmpty();
        assertThat(accountResource.getComment()).isNotEmpty();


    }


    @Test
    public void testModelContainsSlashedComments() {

        for (Operation operation : model.getOperations()) {
            if ("cancellation".equals(operation.getName())) {

                assertThat(operation.getPreamble()).contains("Request the cancellation of a given resource.");


                Attribute attribute = operation.getAttributes().iterator().next();

                assertThat(attribute.getName()).isEqualTo("thruDate");
                assertThat(attribute.getComment()).isEqualTo("The requested cancellation date.");

                return;
            }
        }

        fail("No operation found for cancellation");
    }

}
