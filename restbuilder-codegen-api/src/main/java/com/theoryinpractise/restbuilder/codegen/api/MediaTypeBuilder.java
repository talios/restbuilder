package com.theoryinpractise.restbuilder.codegen.api;


import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.Operation;
import com.theoryinpractise.restbuilder.parser.model.Resource;

public class MediaTypeBuilder {

    // http://www.iana.org/assignments/media-types/application/index.html

    public static String buildContentType(Model model, Operation operation) {
        return String.format("application/vnd.%s.%s+json", model.getNamespace(), operation.getName());
    }

    public static String buildContentType(Model model, Resource resource) {
        return String.format("application/vnd.%s.%s+json", model.getNamespace(), resource.getName());
    }

}
