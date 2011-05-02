package com.theoryinpractise.restbuilder.codegen.api;


import com.theoryinpractise.restbuilder.parser.BaseClassElement;
import com.theoryinpractise.restbuilder.parser.model.Model;

public class MediaTypeBuilder {

    // http://www.iana.org/assignments/media-types/application/index.html

    public static String buildContentType(Model model, BaseClassElement mediaTypeElement) {
        return String
                .format("application/vnd.%s.%s+json", model.getNamespace(), mediaTypeElement.getMediaTypeName())
                .toLowerCase();

    }



}
