package com.theoryinpractise.restbuilder.parser;

import com.theoryinpractise.restbuilder.parser.model.RestModel;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class RestBuilderTest {

    @Test
    public void testBuildModel() throws Exception {


        RestBuilder restBuilder = new RestBuilder();

        RestModel model = restBuilder.buildModel("/account.rbuilder");

        assertThat(model)
                .describedAs("A restbuilder model object")
                .isNotNull()
                .isInstanceOf(RestModel.class);

        assertThat(model.getaPackage()).isEqualTo("com.example.rebuilder");
        assertThat(model.getaNamespace()).isEqualTo("example");
        assertThat(model.getOperations()).isNotEmpty().hasSize(2);


    }
}
