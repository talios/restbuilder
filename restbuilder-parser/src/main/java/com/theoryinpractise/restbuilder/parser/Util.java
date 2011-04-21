package com.theoryinpractise.restbuilder.parser;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static com.google.common.io.Resources.newReaderSupplier;

public class Util {

    public static String readResourceAsString(final String src) throws IOException {
        URL resource = RestBuilder.class.getResource(src);
        InputSupplier<InputStreamReader> is = newReaderSupplier(resource, Charsets.UTF_8);
        return CharStreams.toString(is);
    }


}
