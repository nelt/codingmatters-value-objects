package org.codingmatters.value.objects.js.generator;

import org.codingmatters.value.objects.js.error.ProcessingException;
import org.codingmatters.value.objects.js.parser.model.ParsedValueObject;
import org.codingmatters.value.objects.js.parser.model.ParsedYAMLSpec;
import org.codingmatters.value.objects.js.parser.model.ValueObjectProperty;
import org.codingmatters.value.objects.js.parser.model.types.ObjectTypeExternalValue;
import org.codingmatters.value.objects.js.parser.model.types.ObjectTypeInSpecValueObject;
import org.codingmatters.value.objects.js.parser.model.types.ObjectTypeNested;
import org.codingmatters.value.objects.js.parser.model.types.ValueObjectTypeList;
import org.codingmatters.value.objects.js.parser.model.types.ValueObjectTypePrimitiveType;
import org.codingmatters.value.objects.js.parser.model.types.YamlEnumExternalEnum;
import org.codingmatters.value.objects.js.parser.model.types.YamlEnumInSpecEnum;
import org.codingmatters.value.objects.js.parser.processing.ParsedYamlProcessor;

import java.io.File;
import java.io.IOException;


public class JsSpecProcessor implements ParsedYamlProcessor {

    private final File rootDirectory;
    private final String rootPackage;
    private final GenerationContext generationContext;

    public JsSpecProcessor( File rootDirectory, String rootPackage ) {
        this.rootDirectory = rootDirectory;
        this.rootPackage = rootPackage;
        this.generationContext = new GenerationContext( rootPackage );
    }

    @Override
    public void process( ParsedYAMLSpec spec ) throws ProcessingException {
        rootDirectory.mkdirs();
        for( ParsedValueObject valueObject : spec.valueObjects() ) {
            valueObject.process( this );
        }
    }

    @Override
    public void process( ParsedValueObject valueObject ) throws ProcessingException {
        try {
            String objectName = Naming.firstLetterUpperCase( valueObject.name() );
            String fileName = objectName + ".js";

            File targetDirectory = new File( rootDirectory, generationContext.currentPackagePath() );
            String targetFile = String.join( "/", targetDirectory.getPath(), fileName );
            JsClassWriter write = new JsClassWriter( targetFile );
            generationContext.writer( write );

            for( ValueObjectProperty property : valueObject.properties() ) {
                property.process( this );
            }
            write.line( "import { deepFreezeSeal, FlexDate, FlexDateTime, FlexTime, FlexZonedDateTime } from 'flexio-jshelpers' " ); // TODO don't necessary need all date
            write.line( "class " + objectName + " {" );
            write.generateConstructor( valueObject.properties() );
            write.generateGetters( valueObject.properties() );
            write.generateToObjectMethod( valueObject.properties() );
            write.generateToJsonMethod();
            write.line( "}" );
            write.line( "export { " + objectName + "}" );
            generationContext.write().flush();

            String builderName = objectName + "Builder";
            write.line( "class " + builderName + " {" );
            write.writeLine( "constructor(){}" );
            write.generateSetters( valueObject.properties() );
            write.generateBuildMethod( objectName, valueObject.properties() );
            
        } catch( Exception e ) {
            throw new ProcessingException( "Error processing value object", e );
        }
    }

    @Override
    public void process( ValueObjectProperty property ) throws ProcessingException {
        property.type().process( this );
    }

    @Override
    public void process( ObjectTypeExternalValue externalValueObject ) {
        // TODO import
    }

    @Override
    public void process( ObjectTypeInSpecValueObject inSpecValueObject ) throws ProcessingException {
        try {
            String className = Naming.className( inSpecValueObject.inSpecValueObjectName() );
            String builderName = Naming.builderName( inSpecValueObject.inSpecValueObjectName() );
            String packageName = Naming.findPackage( generationContext.currentPackage(), rootPackage + "." + inSpecValueObject.inSpecValueObjectName() );
            generationContext.write().line( "import { " + className + ", " + builderName + " } from '" + packageName + "'" );
        } catch( IOException e ) {
            throw new ProcessingException( "Error processing in spec value object", e );
        }
    }

    @Override
    public void process( ObjectTypeNested nestedValueObject ) throws ProcessingException {
        try {
            String className = Naming.className( nestedValueObject.nestValueObject().name() );
            String builderName = Naming.builderName( nestedValueObject.nestValueObject().name() );
            String packageName = Naming.findPackage( generationContext.currentPackage(), rootPackage + "." + nestedValueObject.namespace() );
            generationContext.write().line( "import { " + className + ", " + builderName + " } from '" + packageName + "'" );
        } catch( IOException e ) {
            throw new ProcessingException( "Error processing in spec value object", e );
        }
    }

    @Override
    public void process( ValueObjectTypeList list ) throws ProcessingException {
        try {
            String className = Naming.className( list.name() );
            String packageName = Naming.findPackage( generationContext.currentPackage(), rootPackage + "." + list.namespace() + "." + className );
            generationContext.write().line( "import { " + className + " } from '" + packageName + "'" );
        } catch( IOException e ) {
            throw new ProcessingException( "Error processing in spec value object list: " + list.name(), e );
        }
    }

    @Override
    public void process( ValueObjectTypePrimitiveType primitiveType ) {

    }

    @Override
    public void process( YamlEnumExternalEnum externalEnum ) {
//        generationContext.write().line( "import { " + externalEnum.enumReference() + " } " );
    }

    @Override
    public void process( YamlEnumInSpecEnum inSpecEnum ) throws ProcessingException {
        try {
            String className = Naming.className( inSpecEnum.name() );
            String packageName = Naming.findPackage( generationContext.currentPackage(), rootPackage + "." + inSpecEnum.namespace() + "." + className );
            generationContext.write().line( "import { " + className + " } from '" + packageName + "'" );
        } catch( IOException e ) {
            throw new ProcessingException( "Error processing in spec enum", e );
        }
    }
}
