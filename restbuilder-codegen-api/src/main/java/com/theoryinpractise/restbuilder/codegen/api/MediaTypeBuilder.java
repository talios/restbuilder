package com.theoryinpractise.restbuilder.codegen.api;


import com.theoryinpractise.restbuilder.parser.model.RestModel;
import com.theoryinpractise.restbuilder.parser.model.RestOperation;
import com.theoryinpractise.restbuilder.parser.model.RestResource;

public class MediaTypeBuilder {

    // http://www.iana.org/assignments/media-types/application/index.html

    public static String buildContentType(RestModel model, RestOperation operation) {
        return String.format("application/vnd.%s.%s+json", model.getNamespace(), operation.getName());
    }

    public static String buildContentType(RestModel model, RestResource resource) {
        return String.format("application/vnd.%s.%s+json", model.getNamespace(), resource.getName());
    }

}
