package org.codingmatters.value.objects.js.parser.model;

import org.codingmatters.value.objects.js.error.ProcessingException;
import org.codingmatters.value.objects.js.parser.processing.ParsedYamlProcessor;
import org.codingmatters.value.objects.js.parser.processing.ProcessableYaml;

import java.util.ArrayList;
import java.util.List;

public class ParsedValueObject implements ProcessableYaml {

    private final String name;
    private final List<ValueObjectProperty> properties;

    public ParsedValueObject( String name ) {
        this.properties = new ArrayList<>();
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public List<ValueObjectProperty> properties() {
        return this.properties;
    }

    @Override
    public void process( ParsedYamlProcessor processor ) throws ProcessingException {
        processor.process( this );
    }
}