package com.theoryinpractise.restbuilder.parser;

import com.theoryinpractise.restbuilder.parser.model.RestModel;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.IOException;
import java.util.List;

import static com.theoryinpractise.restbuilder.parser.Util.readResourceAsString;


public class RestBuilder {
    public RestModel buildModel(String resource) throws IOException {

        RestBuilderParser parser = Parboiled.createParser(RestBuilderParser.class);

        ParsingResult result = new TracingParseRunner(parser.Expression()).run(readResourceAsString(resource));

        if (result.hasErrors()) {
            List<ParseError> parseErrors = result.parseErrors;
            for (ParseError parseError : parseErrors) {
                System.out.println("Error at " + parseError.getInputBuffer().getPosition(parseError.getStartIndex()).toString());
            }

            throw new IllegalArgumentException("model has errors");
        } else {
            return (RestModel) result.resultValue;
        }

    }






}
