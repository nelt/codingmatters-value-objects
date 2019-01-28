package org.codingmatters.value.objects.js;

import org.codingmatters.value.objects.js.error.ProcessingException;
import org.codingmatters.value.objects.js.generator.packages.PackageFilesBuilder;
import org.codingmatters.value.objects.js.generator.visitor.JsClassGeneratorSpecProcessor;
import org.codingmatters.value.objects.js.parser.model.ParsedYAMLSpec;

import java.io.File;

public class SpecJsGenerator {

    private final ParsedYAMLSpec spec;
    private final File targetDirectory;
    private final String rootPackage;

    public SpecJsGenerator( ParsedYAMLSpec spec, String rootPackage, File targetDirectory ) {
        this.spec = spec;
        this.rootPackage = rootPackage;
        this.targetDirectory = targetDirectory;
    }

    public void generate( PackageFilesBuilder packageFilesBuilder ) throws ProcessingException {
        JsClassGeneratorSpecProcessor processor = new JsClassGeneratorSpecProcessor( targetDirectory, rootPackage, packageFilesBuilder );
        processor.process( spec );
    }
}
