package com.theoryinpractise.restbuilder.parser;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.theoryinpractise.restbuilder.parser.model.Model;
import com.theoryinpractise.restbuilder.parser.model.MultiModel;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.IOException;
import java.util.List;


public class RestBuilder {

    private boolean tracingEnabled = false;

    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public Model buildModel(Iterable<NamedInputSupplier> suppliers) throws IOException {
        List<Model> models = Lists.newArrayList();
        for (NamedInputSupplier supplier : suppliers) {
            Model model = buildModel(supplier);
            models.add(model);
        }

        return new MultiModel(models).resolve();
    }

    private Model buildModel(NamedInputSupplier supplier) throws IOException {

        RestBuilderParser parser = Parboiled.createParser(RestBuilderParser.class);

        ParseRunner parseRunner = isTracingEnabled()
                ? new TracingParseRunner(parser.Expression())
                : new ReportingParseRunner(parser.Expression());

        parser.setName(supplier.getName());
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
