package org.codingmatters.value.objects.generation;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.codingmatters.value.objects.generation.collection.ValueList;
import org.codingmatters.value.objects.generation.collection.ValueListImplementation;
import org.codingmatters.value.objects.generation.collection.ValueSet;
import org.codingmatters.value.objects.generation.collection.ValueSetImplementation;
import org.codingmatters.value.objects.generation.preprocessor.PackagedValueSpec;
import org.codingmatters.value.objects.generation.preprocessor.SpecPreprocessor;
import org.codingmatters.value.objects.spec.PropertyCardinality;
import org.codingmatters.value.objects.spec.PropertySpec;
import org.codingmatters.value.objects.spec.Spec;
import org.codingmatters.value.objects.spec.ValueSpec;

import java.io.File;
import java.io.IOException;

import static org.codingmatters.value.objects.spec.PropertyCardinality.LIST;
import static org.codingmatters.value.objects.spec.PropertyCardinality.SET;

/**
 * Created by nelt on 9/13/16.
 */
public class SpecCodeGenerator {

    private final Spec spec;
    private final String rootPackage;
    private final File rootDirectory;

    public SpecCodeGenerator(Spec spec, String rootPackage, File toDirectory) {
        this.spec = spec;
        this.rootPackage = rootPackage;
        this.rootDirectory = toDirectory;
    }

    public void generate() throws IOException {
        File packageDestination = this.packageDestination(rootDirectory, this.rootPackage);

        if(this.hasPropertyWithCardinality(this.spec, LIST)) {
            TypeSpec valueListInterface = new ValueList(this.rootPackage).type();
            this.writeJavaFile(packageDestination, this.rootPackage, valueListInterface);
            this.writeJavaFile(packageDestination, this.rootPackage, new ValueListImplementation(this.rootPackage, valueListInterface).type());
        }
        if(this.hasPropertyWithCardinality(this.spec, SET)) {
            TypeSpec valueSetInterface = new ValueSet(this.rootPackage).type();
            this.writeJavaFile(packageDestination, this.rootPackage, valueSetInterface);
            this.writeJavaFile(packageDestination, this.rootPackage, new ValueSetImplementation(this.rootPackage, valueSetInterface).type());
        }

        for (PackagedValueSpec valueSpec : new SpecPreprocessor(this.spec, this.rootPackage).packagedValueSpec()) {
            this.generateValueTypesTo(valueSpec);
        }
    }

    private File packageDestination(File dir, String pack) {
        return new File(dir, pack.replaceAll(".", "/"));
    }

    private boolean hasPropertyWithCardinality(Spec spec, PropertyCardinality cardinality) {
        for (ValueSpec valueSpec : spec.valueSpecs()) {
            for (PropertySpec propertySpec : valueSpec.propertySpecs()) {
                if(cardinality.equals(propertySpec.typeSpec().cardinality())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void generateValueTypesTo(PackagedValueSpec packagedValueSpec) throws IOException {
        File packageDestination = this.packageDestination(this.rootDirectory, packagedValueSpec.packagename());
        packageDestination.mkdirs();

        ValueConfiguration types = new ValueConfiguration(this.rootPackage, packagedValueSpec.packagename(), packagedValueSpec.valueSpec());

        TypeSpec valueInterface = new ValueInterface(types, packagedValueSpec.valueSpec().propertySpecs()).type();
        this.writeJavaFile(packageDestination, packagedValueSpec.packagename(), valueInterface);

        TypeSpec valueImpl = new ValueImplementation(types, packagedValueSpec.valueSpec().propertySpecs()).type();
        this.writeJavaFile(packageDestination, packagedValueSpec.packagename(), valueImpl);
    }


    private void writeJavaFile(File packageDestination, String pack, TypeSpec type) throws IOException {
        JavaFile file = JavaFile.builder(pack, type).build();
        file.writeTo(packageDestination);
        if(System.getProperty("spec.code.generator.debug", "false").equals("true")) {
            file.writeTo(System.out);
        }
    }

}
