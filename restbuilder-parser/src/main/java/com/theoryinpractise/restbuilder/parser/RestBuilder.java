package com.theoryinpractise.restbuilder.parser;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.theoryinpractise.restbuilder.parser.model.Model;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;


public class RestBuilder {

    private boolean tracingEnabled = false;

    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public Model buildModel(URL url) throws IOException {
        return buildModel(com.google.common.io.Resources.newReaderSupplier(url, Charsets.UTF_8));
    }

    public Model buildModel(File file) throws IOException {
        return buildModel(Files.newReaderSupplier(file, Charsets.UTF_8));
    }

    private Model buildModel(InputSupplier<InputStreamReader> supplier) throws IOException {

        RestBuilderParser parser = Parboiled.createParser(RestBuilderParser.class);

        ParseRunner parseRunner = isTracingEnabled()
                ? new TracingParseRunner(parser.Expression())
                : new ReportingParseRunner(parser.Expression());

        ParsingResult result = parseRunner.run(CharStreams.toString(supplier));

        if (result.hasErrors()) {
            List<ParseError> parseErrors = result.parseErrors;
            for (ParseError parseError : parseErrors) {
                System.out.println("Error at " + parseError.getInputBuffer().getPosition(parseError.getStartIndex()).toString());
            }

            throw new IllegalArgumentException("model has errors");
        } else {
            return (Model) result.resultValue;
        }

    }

}
