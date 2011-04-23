package com.theoryinpractise.restbuilder.parser;

import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Resource;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class RestBuilderTest {

    @Test
    public void testBuildModel() throws Exception {


        RestBuilder restBuilder = new RestBuilder();
//        restBuilder.setTracingEnabled(true);

        Model model = restBuilder.buildModel(RestBuilderTest.class.getResource("/account.rbuilder"));

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
}
