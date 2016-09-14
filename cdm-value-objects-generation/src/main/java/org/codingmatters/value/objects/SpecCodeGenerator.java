package org.codingmatters.value.objects;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codingmatters.value.objects.spec.PropertySpec;
import org.codingmatters.value.objects.spec.Spec;
import org.codingmatters.value.objects.spec.ValueSpec;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static javax.lang.model.element.Modifier.*;

/**
 * Created by nelt on 9/13/16.
 */
public class SpecCodeGenerator {

    private final Spec spec;
    private final String packageName;

    public SpecCodeGenerator(Spec spec, String packageName) {
        this.spec = spec;
        this.packageName = packageName;
    }

    public void generateTo(File dir) throws IOException {
        File packageDestination = new File(dir, packageName.replaceAll(".", "/"));

        for (ValueSpec valueSpec : this.spec.valueSpecs()) {
            this.generateValueTypesTo(valueSpec, packageDestination);
        }
    }

    private void generateValueTypesTo(ValueSpec valueSpec, File packageDestination) throws IOException {
        String interfaceName = this.capitalizedFirst(valueSpec.name());

        TypeSpec valueBuilder = this.createValueBuilder(interfaceName, valueSpec.propertySpecs());
        TypeSpec valueInterface = this.createValueInterface(interfaceName, valueBuilder, valueSpec.propertySpecs());
        TypeSpec valueImpl = this.createValueImplementation(interfaceName, valueSpec.propertySpecs());

        this.writeJavaFile(packageDestination, valueInterface);
        this.writeJavaFile(packageDestination, valueImpl);
    }


    private TypeSpec createValueInterface(String interfaceName, TypeSpec valueBuilder, List<PropertySpec> propertySpecs) {
        List<MethodSpec> getters = new LinkedList<>();

        for (PropertySpec propertySpec : propertySpecs) {
            getters.add(
                    MethodSpec.methodBuilder(propertySpec.name())
                            .returns(ClassName.bestGuess(propertySpec.type()))
                            .addModifiers(PUBLIC, ABSTRACT)
                            .build()
            );
        }

        return TypeSpec.interfaceBuilder(interfaceName)
                    .addModifiers(PUBLIC)
                    .addMethods(getters)
                    .addType(valueBuilder)
                    .build();
    }

    private TypeSpec createValueBuilder(String interfaceName, List<PropertySpec> propertySpecs) {
        List<MethodSpec> setters = new LinkedList<>();

        for (PropertySpec propertySpec : propertySpecs) {
            setters.add(
                    MethodSpec.methodBuilder(propertySpec.name())
                            .addParameter(ClassName.bestGuess(propertySpec.type()), propertySpec.name())
                            .returns(ClassName.bestGuess("Builder"))
                            .addModifiers(PUBLIC)
                            .addStatement("return this")
                            .build()
            );
        }

        MethodSpec builderMethod = MethodSpec.methodBuilder("builder")
                .addModifiers(STATIC, PUBLIC)
                .returns(ClassName.bestGuess("Builder"))
                .addStatement("return new $T()", ClassName.bestGuess("Builder"))
                .build();
        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(PUBLIC)
                .returns(ClassName.bestGuess(interfaceName))
                .addStatement("return new $T()", ClassName.bestGuess(interfaceName + "Impl"))
                .build();

        return TypeSpec.classBuilder("Builder")
                .addModifiers(PUBLIC, STATIC)
                .addMethod(builderMethod)
                .addMethod(buildMethod)
                .addMethods(setters)
                .build();
    }

    private TypeSpec createValueImplementation(String interfaceName, List<PropertySpec> propertySpecs) {
        List<MethodSpec> getters = new LinkedList<>();

        for (PropertySpec propertySpec : propertySpecs) {
            getters.add(
                    MethodSpec.methodBuilder(propertySpec.name())
                            .returns(ClassName.bestGuess(propertySpec.type()))
                            .addModifiers(PUBLIC)
                            .addStatement("return null")
                            .build()
            );
        }
        return TypeSpec.classBuilder(interfaceName + "Impl")
                .addSuperinterface(ClassName.get(this.packageName, interfaceName))
                .addMethods(getters)
                .build();
    }

    private void writeJavaFile(File packageDestination, TypeSpec valueInterface) throws IOException {
        JavaFile file = JavaFile.builder(this.packageName, valueInterface).build();
        file.writeTo(packageDestination);
    }

    private String capitalizedFirst(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
