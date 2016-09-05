package org.codingmatters.value.objects.spec;

import java.util.Objects;

/**
 * Created by nelt on 9/5/16.
 */
public class TypeSpec {

    static public Builder type() {
        return new Builder();
    }

    static public class Builder {
        private String typeRef;
        private TypeKind typeKind;

        public Builder typeRef(String type) {
            this.typeRef = type;
            return this;
        }

        public Builder typeKind(TypeKind typeKind) {
            this.typeKind = typeKind;
            return this;
        }

        public TypeSpec build() {
            return new TypeSpec(this.typeRef, this.typeKind);
        }
    }

    private final String typeRef;
    private final TypeKind typeKind;

    private TypeSpec(String typeRef, TypeKind typeKind) {
        this.typeRef = typeRef;
        this.typeKind = typeKind;
    }

    public String typeRef() {
        return typeRef;
    }

    public TypeKind typeKind() {
        return typeKind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeSpec typeSpec = (TypeSpec) o;
        return Objects.equals(typeRef, typeSpec.typeRef) &&
                typeKind == typeSpec.typeKind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeRef, typeKind);
    }

    @Override
    public String toString() {
        return "TypeSpec{" +
                "typeRef='" + typeRef + '\'' +
                ", typeKind=" + typeKind +
                '}';
    }
}