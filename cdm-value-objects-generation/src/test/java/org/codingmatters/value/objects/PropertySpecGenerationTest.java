package org.codingmatters.value.objects;

import org.codingmatters.tests.compile.CompiledCode;
import org.codingmatters.value.objects.spec.Spec;
import org.codingmatters.value.objects.spec.TypeKind;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.codingmatters.tests.reflect.ReflectMatchers.*;
import static org.codingmatters.value.objects.spec.PropertySpec.property;
import static org.codingmatters.value.objects.spec.PropertyTypeSpec.type;
import static org.codingmatters.value.objects.spec.Spec.spec;
import static org.codingmatters.value.objects.spec.ValueSpec.valueSpec;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by nelt on 9/14/16.
 */
public class PropertySpecGenerationTest {

    @Rule
    public TemporaryFolder dir = new TemporaryFolder();

    private final Spec spec  = spec()
            .addValue(
                    valueSpec().name("val")
                    .addProperty(property().name("prop").type(type().typeKind(TypeKind.JAVA_TYPE).typeRef(String.class.getName())))
                    .addProperty(property().name("prop2").type(type().typeKind(TypeKind.JAVA_TYPE).typeRef(String.class.getName())))
            )
            .build();
    private CompiledCode compiled;

    @Before
    public void setUp() throws Exception {
        new SpecCodeGenerator(this.spec, "org.generated").generateTo(dir.getRoot());
        this.compiled = CompiledCode.compile(this.dir.getRoot());
    }

    @Test
    public void multipleProperty_multipleMethods() throws Exception {
        assertThat(compiled.getClass("org.generated.Val"), is(anInterface().with(aMethod().named("prop"))));
        assertThat(compiled.getClass("org.generated.ValImpl"), is(aClass().with(aMethod().named("prop"))));
        assertThat(compiled.getClass("org.generated.Val$Builder"), is(aClass().with(aMethod().named("prop"))));


        assertThat(compiled.getClass("org.generated.Val"), is(anInterface().with(aMethod().named("prop2"))));
        assertThat(compiled.getClass("org.generated.ValImpl"), is(aClass().with(aMethod().named("prop2"))));
        assertThat(compiled.getClass("org.generated.Val$Builder"), is(aClass().with(aMethod().named("prop"))));
    }
}
