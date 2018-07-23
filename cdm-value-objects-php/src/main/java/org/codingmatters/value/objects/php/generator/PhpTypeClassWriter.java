package org.codingmatters.value.objects.php.generator;


import org.codingmatters.value.objects.php.phpmodel.PhpEnum;
import org.codingmatters.value.objects.php.phpmodel.PhpMethod;
import org.codingmatters.value.objects.php.phpmodel.PhpPackagedValueSpec;
import org.codingmatters.value.objects.php.phpmodel.PhpPropertySpec;
import org.codingmatters.value.objects.spec.PropertyCardinality;
import org.codingmatters.value.objects.spec.TypeKind;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PhpTypeClassWriter {

    public final String packageName;
    private final File targetDirectory;
    private final String fileName;
    public final BufferedWriter writer;
    public final String objectName;
    private final Set<String> imports;
    private final String indent = "    ";

    public PhpTypeClassWriter( File targetDirectory, String packageName, String name ) throws IOException {
        if( (!targetDirectory.exists() && !targetDirectory.mkdirs()) || !targetDirectory.isDirectory() ) {
            throw new IOException( "Target directory not exist or is not a directory" );
        }
        this.packageName = packageName;
        this.targetDirectory = targetDirectory;
        this.objectName = firstLetterUpperCase( name );
        this.fileName = objectName + ".php";
        String targetFile = String.join( "/", this.targetDirectory.getPath(), fileName );
        System.out.println( "Generating in " + targetFile );
        this.writer = new BufferedWriter( new FileWriter( targetFile ) );
        this.imports = new HashSet<>();
    }


    public void writeEnum( PhpEnum enumValue, Map<String, String> classReferencesContext ) throws IOException {
        putClassInContext( enumValue.name(), classReferencesContext );
        startPhpFile();

        writer.write( "use \\Exception;" );
        twoLine( 0 );
        writer.write( "use \\JsonSerializable;" );
        twoLine( 0 );

        writer.write( "class " + enumValue.name() + " implements JsonSerializable {" );
        twoLine( 1 );
        writer.write( "protected $value;" );
        twoLine( 1 );
        writer.write( "private function __construct( $value ){" );
        newLine( 2 );
        writer.write( "$this->value = $value;" );
        newLine( 1 );
        writer.write( "}" );
        twoLine( 1 );
        writer.write( "public function value(){" );
        newLine( 2 );
        writer.write( "return $this->value;" );
        newLine( 1 );
        writer.write( "}" );
        newLine( 1 );

        for( String value : enumValue.enumValues() ) {
            newLine( 1 );
            writer.write( "public static function " + value + "(): " + enumValue.name() + " { " );
            writer.newLine();
            indent( 2 );
            writer.write( "return new " + enumValue.name() + "( '" + value + "' );" );
            writer.newLine();
            indent( 1 );
            writer.write( "}" );
            newLine( 0 );
        }
        newLine( 1 );
        writer.write( "public static function valueOf( string $value ): " + enumValue.name() + " {" );
        newLine( 2 );
        writer.write( "if( in_array($value, BookKind::values())){" );
        newLine( 3 );
        writer.write( "return new BookKind( $value );" );
        newLine( 2 );
        writer.write( "} else {" );
        newLine( 3 );
        writer.write( "throw new Exception( 'No enum constant '.$value );" );
        newLine( 2 );
        writer.write( "}" );
        newLine( 1 );
        writer.write( "}" );
        twoLine( 1 );

        writer.write( "public static function values(){" );
        newLine( 2 );
        writer.write( "return array('" + String.join( "', '", enumValue.enumValues() ) + "');" );
        newLine( 1 );
        writer.write( "}" );

        newLine( 1 );
        writer.write( "public function jsonSerialize() {" );
        newLine( 2 );
        writer.write( "return $this->value;" );
        newLine( 1 );
        writer.write( "}" );

        newLine( 0 );
        writer.write( "}" );

        writer.flush();
        writer.close();
    }

    private void putClassInContext( String name, Map<String, String> classReferencesContext ) {
        if( !classReferencesContext.containsKey( name ) ) {
            classReferencesContext.put( name, this.packageName + "." + name );
        }
    }

    private void startPhpFile() throws IOException {
        writer.write( "<?php" );
        twoLine( 0 );

        writer.write( "namespace " + String.join( "\\", this.packageName.split( "\\." ) ) + ";" );
        twoLine( 0 );
    }

    public void writeValueObject( PhpPackagedValueSpec spec, Map<String, String> classReferencesContext ) throws IOException {
        putClassInContext( this.objectName, classReferencesContext );
        startPhpFile();

        for( String importation : spec.imports() ) {
            writer.write( "use " + importation.replace( ".", "\\" ) + ";" );
            writer.newLine();
        }
        writer.newLine();

        writer.write( "class " + this.objectName );
        if( spec.extender() != null ) {
            writer.write( " extends " + spec.extender().typeRef() );
        }
        writer.write( " {" );
        twoLine( 1 );

        for( PhpPropertySpec fieldSpec : spec.propertySpecs() ) {
            writer.write( "private $" + getFieldName( fieldSpec ) + ";" );
            newLine( 1 );
        }
        newLine( 0 );

        for( PhpMethod phpMethod : spec.methods() ) {
            indent( 1 );
            writer.write( "public function " );
            writer.write( phpMethod.name() );
            writer.write( "(" );
            writer.write( String.join( ", ", phpMethod.parameters().stream().map( param->param.type() + " $" + param.name() ).collect( Collectors.toList() ) ) );
            writer.write( ")" );
            String returnType = phpMethod.type();
            if( returnType != null ) {
                writer.write( ": " + returnType );
            }
            writer.write( " {" );
            writer.newLine();
            for( String instruction : phpMethod.instructions() ) {
                indent( 2 );
                writer.write( instruction );
                writer.write( ";" );
                writer.newLine();
            }
            indent( 1 );
            writer.write( "}" );
            twoLine( 0 );
        }
        writer.write( "}" );

        writer.flush();
        writer.close();
    }

    public void writeReader( PhpPackagedValueSpec spec ) throws IOException {
        startPhpFile();

        for( String importation : spec.imports() ) {
            writer.write( "use " + importation.replace( ".", "\\" ) + ";" );
            writer.newLine();
        }
        twoLine( 0 );
        writer.write( "class " + this.objectName + "Reader {" );
        twoLine( 1 );
        writer.write( "public function read( string $json ) : " + this.objectName + " {" );
        newLine( 2 );
        writer.write( "$decode = json_decode( $json );" );
        newLine( 2 );
        String resultVar = "$" + firstLetterLowerCase( this.objectName );
        writer.write( resultVar + " = new " + this.objectName + "();" );
        newLine( 2 );
        for( PhpPropertySpec property : spec.propertySpecs() ) {
            if( property.typeSpec().cardinality() == PropertyCardinality.LIST || property.typeSpec().cardinality() == PropertyCardinality.SET ) {
                processFieldList( resultVar, property );
            } else {
                processReadSingleField( resultVar, property );
            }
        }
        writer.write( "return $decode;" );

        newLine( 1 );
        writer.write( "}" );

        twoLine( 0 );
        writer.write( "}" );
        writer.flush();
        writer.close();
    }

    private void processFieldList( String resultVar, PhpPropertySpec property ) throws IOException {
        writer.write( "if( isset( $decode['" + property.name() + "'] )){" );
        newLine( 3 );
        writer.write( "$list = array();" );
        newLine( 3 );
        writer.write( "foreach( $decode['" + property.name() + "'] as $item ){" );
        newLine( 4 );
        writer.write( "$list[] = $item;" );
        newLine( 3 );
        writer.write( "}" );
        newLine( 3 );
        writer.write( resultVar + "->with" + firstLetterUpperCase( property.name() ) + "( " );
        if( property.typeSpec().typeKind() == TypeKind.ENUM ) {
            writer.write( property.typeSpec().typeRef() + ".valueOf( $decode['" + property.name() + "'] )" );
        } else if( property.typeSpec().typeKind() == TypeKind.JAVA_TYPE && isDate( property ) ) {
            writer.write( "FlexDate::new" + getDateClass( property.typeSpec().typeRef() ) + "( $decode['" + property.name() + "'] )" );
        } else {
            writer.write( "$decode['" + property.name() + "']" );
        }
        writer.write( " );" );
        newLine( 2 );
        writer.write( "}" );
        newLine( 2 );
    }

    private void processReadSingleField( String resultVar, PhpPropertySpec property ) throws IOException {
        if( property.typeSpec().typeKind() == TypeKind.ENUM ) {
            writer.write( "if( isset( $decode['" + property.name() + "'] )){" );
            newLine( 3 );
            writer.write( resultVar + "->with" + firstLetterUpperCase( property.name() ) + "( " + property.typeSpec().typeRef() + ".valueOf( $decode['" + property.name() + "'] )));" );
            newLine( 2 );
            writer.write( "}" );
            newLine( 2 );
        } else if( property.typeSpec().typeKind() == TypeKind.JAVA_TYPE && isDate( property ) ) {
            writer.write( "if( isset( $decode['" + property.name() + "'] )){" );
            newLine( 3 );
            writer.write( resultVar + "->with" + firstLetterUpperCase( property.name() ) + "( FlexDate::new" + getDateClass( property.typeSpec().typeRef() ) + "( $decode['" + property.name() + "'] )));" );
            newLine( 2 );
            writer.write( "}" );
            newLine( 2 );
        } else {
            writer.write( "if( isset( $decode['" + property.name() + "'] )){" );
            newLine( 3 );
            writer.write( resultVar + "->with" + firstLetterUpperCase( property.name() ) + "( $decode['" + property.name() + "'] ));" );
            newLine( 2 );
            writer.write( "}" );
            newLine( 2 );
        }
    }

    private String getDateClass( String dateClass ) {
        return String.join( "", Arrays.stream( dateClass.split( "-" ) ).map( this::firstLetterUpperCase ).collect( Collectors.toList() ) );
    }

    private boolean isDate( PhpPropertySpec property ) {
        return property.typeSpec().typeRef().equals( "date" ) || property.typeSpec().typeRef().equals( "time" ) || property.typeSpec().typeRef().equals( "date-time" );
    }

    private void twoLine( int indentSize ) throws IOException {
        newLine( 0 );
        newLine( indentSize );
    }

    private void newLine( int indentSize ) throws IOException {
        writer.newLine();
        indent( indentSize );
    }

    private void indent( int indent ) throws IOException {
        for( int i = 0; i < indent; i++ ) {
            writer.write( this.indent );
        }
    }

    private String getFieldName( PhpPropertySpec fieldSpec ) {
        if( fieldSpec.name().equals( "$list" ) ) {
            return fieldSpec.typeSpec().typeRef() + "s";
        }
        return fieldSpec.name();
    }

    private String firstLetterUpperCase( String name ) {
        return name.substring( 0, 1 ).toUpperCase( Locale.ENGLISH ) + name.substring( 1 );
    }

    private String firstLetterLowerCase( String name ) {
        return name.substring( 0, 1 ).toLowerCase( Locale.ENGLISH ) + name.substring( 1 );
    }
}