package org.codingmatters.value.objects;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.codingmatters.value.objects.exception.LowLevelSyntaxException;
import org.codingmatters.value.objects.exception.SpecSyntaxException;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;

import static org.codingmatters.value.objects.spec.PropertySpec.property;
import static org.codingmatters.value.objects.spec.Spec.spec;
import static org.codingmatters.value.objects.spec.ValueSpec.valueSpec;
import static org.codingmatters.value.objects.utils.Utils.streamFor;
import static org.codingmatters.value.objects.utils.Utils.string;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nelt on 9/3/16.
 */
public class SpecReaderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SpecReader reader = new SpecReader();

    @Test
    public void lowLevelSyntaxError() throws Exception {
        this.exception.expect(LowLevelSyntaxException.class);
        this.exception.expectMessage("spec must be valid YAML expression");
        this.exception.expectCause(Matchers.isA(JsonMappingException.class));

        try(InputStream in = streamFor(string()
                .line("val")
                .line("  prop string")
                .build())) {
            reader.read(in);
        }
    }

    @Test
    public void malformedPropertyName() throws Exception {
        this.exception.expect(SpecSyntaxException.class);
        this.exception.expectMessage("malformed property name \"val/prop erty\" : should be a valid java identifier");

        try(InputStream in = streamFor(string()
                .line("val:")
                .line("  prop erty: string")
                .build())) {
            reader.read(in);
        }
    }

    @Test
    public void invalidType() throws Exception {
        this.exception.expect(SpecSyntaxException.class);
        this.exception.expectMessage("invalid type for property \"val/prop\" : strrrrring, should be one of ");

        try(InputStream in = streamFor(string()
                .line("val:")
                .line("  prop: strrrrring")
                .build())) {
            reader.read(in);
        }
    }

    @Test
    public void manySimpleValueSpec() throws Exception {
        try(InputStream in = streamFor(string()
                .line("val1:")
                .line("val2:")
                .build())) {
            assertThat(
                    reader.read(in),
                    is(
                            spec()
                                    .addValue(valueSpec().name("val1"))
                                    .addValue(valueSpec().name("val2"))
                            .build()
                    )
            );
        }
    }

    @Test
    public void valueWithStringProperties() throws Exception {
        try(InputStream in = streamFor(string()
                .line("val:")
                .line("  p1: string")
                .line("  p2: string")
                .build())) {
            assertThat(
                    reader.read(in),
                    is(
                            spec()
                                    .addValue(valueSpec().name("val")
                                            .addProperty(property().name("p1").type(String.class.getName()))
                                            .addProperty(property().name("p2").type(String.class.getName()))
                                    )
                                    .build()
                    )
            );
        }
    }

    @Test
    public void propertyWithImplementationType() throws Exception {
        try(InputStream in = streamFor(string()
                .line("val1:")
                .line("  p: java.lang.String")
                .build())) {
            assertThat(
                    reader.read(in),
                    is(
                            spec()
                                    .addValue(valueSpec().name("val1")
                                            .addProperty(property().name("p").type("java.lang.String"))
                                    )
                                    .build()
                    )
            );
        }
    }

    @Test
    public void propertyWithInSpecValueObjectType() throws Exception {
        try(InputStream in = streamFor(string()
                .line("val1:")
                .line("  p: $val2")
                .line("val2:")
                .build())) {
            assertThat(
                    reader.read(in),
                    is(
                            spec()
                                    .addValue(valueSpec().name("val1")
                                            .addProperty(property().name("p").type("#ref(val2)"))
                                    )
                                    .addValue(valueSpec().name("val2"))
                                    .build()
                    )
            );
        }
    }

    @Test
    public void propertyWithInSpecValueObjectType_notDeclared_throwsSpecSyntaxException() throws Exception {
        this.exception.expect(SpecSyntaxException.class);
        this.exception.expectMessage("undeclared referenced type for \"val1/p\" : a referenced type should be declared in the same spec");

        try(InputStream in = streamFor(string()
                .line("val1:")
                .line("  p: $val2")
                .build())) {
            reader.read(in);
        }
    }
}
