package org.codingmatters.value.objects.json.property.statement;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.codingmatters.value.objects.json.property.SimplePropertyReaderProducer;
import org.codingmatters.value.objects.spec.PropertySpec;

/**
 * Created by nelt on 6/8/17.
 */
public class EnumPropertyStatement implements PropertyStatement {
    @Override
    public void addSingleStatement(MethodSpec.Builder method, PropertySpec propertySpec, SimplePropertyReaderProducer propertyReaderProducer) {
        method
                .beginControlFlow("try")
                .addStatement("builder.$L(this.readValue(parser, jsonParser -> $T.valueOf(jsonParser.$L()), $S, expectedTokens))",
                        propertySpec.name(),
                        ClassName.bestGuess(propertySpec.typeSpec().typeRef()),
                        propertyReaderProducer.parserMethod(),
                        propertySpec.name()
                )
                .nextControlFlow("catch($T e)", IllegalArgumentException.class)
                .addStatement("" +
                        "throw new IOException(\"error reading enum property $L, value is not one of the enum constants.\", e)",
                        propertySpec.name())
                .endControlFlow();
    }

    @Override
    public void addMultipleStatement(MethodSpec.Builder method, PropertySpec propertySpec, SimplePropertyReaderProducer propertyReaderProducer) {
        method
                .beginControlFlow("try")
                .addStatement("builder.$L(this.readListValue(parser, jsonParser -> $T.valueOf(jsonParser.$L()), $S))",
                        propertySpec.name(),
                        ClassName.bestGuess(propertySpec.typeSpec().typeRef()),
                        propertyReaderProducer.parserMethod(),
                        propertySpec.name()
                )
                .nextControlFlow("catch($T e)", IllegalArgumentException.class)
                .addStatement("" +
                                "throw new IOException(\"error reading enum property $L, value is not one of the enum constants.\", e)",
                        propertySpec.name())
                .endControlFlow();
    }
}