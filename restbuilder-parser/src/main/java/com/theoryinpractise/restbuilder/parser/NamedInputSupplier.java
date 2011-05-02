package com.theoryinpractise.restbuilder.parser;

import com.google.common.base.Charsets;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class NamedInputSupplier implements InputSupplier<InputStreamReader> {

    private String name;
    private InputSupplier<InputStreamReader> inputSupplier;

    public NamedInputSupplier(String name, URL url) {
        this.name = name;
        this.inputSupplier = Resources.newReaderSupplier(url, Charsets.UTF_8);
    }

    public NamedInputSupplier(String name, InputSupplier<InputStreamReader> inputSupplier) {
        this.name = name;
        this.inputSupplier = inputSupplier;
    }

    public String getName() {
        return name;
    }

    @Override
    public InputStreamReader getInput() throws IOException {
        return inputSupplier.getInput();
    }
}
