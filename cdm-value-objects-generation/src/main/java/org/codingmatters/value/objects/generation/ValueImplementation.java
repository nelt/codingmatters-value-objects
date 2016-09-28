package org.codingmatters.value.objects.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codingmatters.value.objects.spec.PropertySpec;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.*;
import static org.codingmatters.value.objects.generation.SpecCodeGenerator.capitalizedFirst;

/**
 * Created by nelt on 9/22/16.
 */
public class ValueImplementation {

    private final ValueObjectConfiguration types;
    private final List<PropertySpec> propertySpecs;

    private final MethodSpec constructor;
    private final List<FieldSpec> fields;
    private final List<MethodSpec> getters;
    private final List<MethodSpec> withers;
    private final MethodSpec equalsMethod;
    private final MethodSpec hashCodeMethod;
    private final MethodSpec toStringMethod;

    public ValueImplementation(ValueObjectConfiguration types, List<PropertySpec> propertySpecs) {
        this.types = types;
        this.propertySpecs = propertySpecs;

        this.constructor = this.createConstructor();
        this.fields = this.createFields();
        this.getters = this.createGetters();
        this.withers = this.createWithers();
        this.equalsMethod = this.createEquals();
        this.hashCodeMethod = this.createHashCode();
        this.toStringMethod = this.createToString();
    }

    public TypeSpec type() {
        return TypeSpec.classBuilder(this.types.valueImplType())
                .addSuperinterface(this.types.valueType())
                .addModifiers(PUBLIC)
                .addMethod(this.constructor)
                .addFields(this.fields)
                .addMethods(this.getters)
                .addMethods(this.withers)
                .addMethod(this.equalsMethod)
                .addMethod(this.hashCodeMethod)
                .addMethod(this.toStringMethod)
                .build();
    }

    private MethodSpec createConstructor() {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

        for (PropertySpec propertySpec : propertySpecs) {
            constructorBuilder
                    .addParameter(this.types.propertyType(propertySpec), propertySpec.name())
                    .addStatement("this.$N = $N", propertySpec.name(), propertySpec.name())
            ;
        }

        return constructorBuilder.build();
    }

    private List<FieldSpec> createFields() {
        List<FieldSpec> fields = new LinkedList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            fields.add(
                    FieldSpec.builder(this.types.propertyType(propertySpec), propertySpec.name(), PRIVATE, FINAL).build()
            );
        }
        return fields;
    }

    private List<MethodSpec> createGetters() {
        List<MethodSpec> getters = new LinkedList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            getters.add(
                    MethodSpec.methodBuilder(propertySpec.name())
                            .returns(this.types.propertyType(propertySpec))
                            .addModifiers(PUBLIC)
                            .addStatement("return this.$N", propertySpec.name())
                            .build()
            );
        }
        return getters;
    }

    private MethodSpec createEquals() {
        /*
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertySpec that = (PropertySpec) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type);
         */

        String statement;
        List<Object> bindings= new LinkedList<>();
        if(propertySpecs.size() > 0) {
            statement = "$T that = ($T) o;\n";
            bindings.add(this.types.valueImplType());
            bindings.add(this.types.valueImplType());

            statement += "return ";
            boolean started = false;
            for (PropertySpec propertySpec : propertySpecs) {
                if(started) {
                    statement += " && \n";
                }
                started = true;

                statement += "$T.equals(this." + propertySpec.name() + ", that." + propertySpec.name() + ")";
                bindings.add(ClassName.get(Objects.class));
            }

        } else {
            statement = "return true";
        }

        return MethodSpec.methodBuilder("equals")
                .addModifiers(PUBLIC)
                .addParameter(ClassName.bestGuess(Object.class.getName()), "o")
                .returns(boolean.class)
                .addAnnotation(ClassName.get(Override.class))
                .addStatement("if (this == o) return true")
                .addStatement("if (o == null || getClass() != o.getClass()) return false")
                .addStatement(statement, bindings.toArray())
                .build();
    }

    private MethodSpec createHashCode() {

        String statement = propertySpecs.stream()
                .map(propertySpec -> "this." + propertySpec.name())
                .collect(Collectors.joining(
                        ", ",
                        "return $T.hash(",
                        ")"
                ));

        return MethodSpec.methodBuilder("hashCode")
                .addModifiers(PUBLIC)
                .returns(int.class)
                .addAnnotation(ClassName.get(Override.class))
                .addStatement(statement, ClassName.get(Objects.class))
                .build();
    }

    private MethodSpec createToString() {
        String statement =
                propertySpecs.stream()
                        .map(propertySpec -> "\"" + propertySpec.name() + "=\" + this." + propertySpec.name() + " +\n")
                        .collect(Collectors.joining(
                                "\", \" + ",
                                "return \"" + this.types.valueType().simpleName() + "{\" +\n",
                                "'}'"
                                )
                        )
                ;
        return MethodSpec.methodBuilder("toString")
                .addModifiers(PUBLIC)
                .returns(String.class)
                .addAnnotation(Override.class)
                .addStatement(statement)
                .build();
    }


    private List<MethodSpec> createWithers() {
        List<MethodSpec> result = new LinkedList<>();

        for (PropertySpec propertySpec : propertySpecs) {
            if(propertySpec.typeKind().isValueObject()) {
                result.add(
                        MethodSpec.methodBuilder("with" + capitalizedFirst(propertySpec.name()))
                                .returns(this.types.valueType())
                                .addModifiers(PUBLIC)
                                .addParameter(this.types.builderPropertyType(propertySpec), "value")
                                .addStatement("return $T.from(this)." + propertySpec.name() + "(value).build()", this.types.builderType())
                                .build()
                );
            } else {
                result.add(
                        MethodSpec.methodBuilder("with" + capitalizedFirst(propertySpec.name()))
                                .returns(this.types.valueType())
                                .addModifiers(PUBLIC)
                                .addParameter(this.types.propertyType(propertySpec), "value")
                                .addStatement("return $T.from(this)." + propertySpec.name() + "(value).build()", this.types.builderType())
                                .build()
                );
            }
        }
        return result;
    }
}
