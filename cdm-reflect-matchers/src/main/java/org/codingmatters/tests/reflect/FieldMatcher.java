package org.codingmatters.tests.reflect;

import org.codingmatters.tests.reflect.utils.MatcherChain;
import org.codingmatters.tests.reflect.utils.MemberDeleguate;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Field;

/**
 * Created by nelt on 9/11/16.
 */
public class FieldMatcher extends TypeSafeMatcher<Field> {

    static FieldMatcher field(ReflectMatcherConfiguration builder) {
        return new FieldMatcher().configure(builder);
    }

    private FieldMatcher configure(ReflectMatcherConfiguration builder) {
        if(builder.levelModifier().equals(LevelModifier.INSTANCE)) {
            this.instance();
        } else {
            this.static_();
        }

        switch (builder.accessModifier()) {
            case PUBLIC:
                this.public_();
                break;
            case PRIVATE:
                this.private_();
                break;
            case PROTECTED:
                this.protected_();
                break;
            case PACKAGE_PRIVATE:
                this.packagePrivate();
                break;
        }
        return this;
    }


    private final MatcherChain<Field> matchers = new MatcherChain<>();
    private final MemberDeleguate<FieldMatcher> memberDeleguate;

    private FieldMatcher() {
        this.memberDeleguate = new MemberDeleguate<>(this.matchers);
    }


    public FieldMatcher named(String name) {
        return this.memberDeleguate.named(name, this);
    }

    private FieldMatcher static_() {
        return this.memberDeleguate.static_(this);
    }

    private FieldMatcher instance() {
        return this.memberDeleguate.notStatic(this);
    }

    private FieldMatcher public_() {
        return this.memberDeleguate.public_(this);
    }

    private FieldMatcher private_() {
        return this.memberDeleguate.private_(this);
    }

    private FieldMatcher protected_() {
        return this.memberDeleguate.protected_(this);
    }

    private FieldMatcher packagePrivate() {
        return this.memberDeleguate.packagePrivate(this);
    }

    public FieldMatcher final_() {
        return this.memberDeleguate.final_(this);
    }

    public FieldMatcher withType(Class type) {
        this.matchers.addMatcher("field type", item -> item.getType().equals(type));
        return this;
    }



    @Override
    protected boolean matchesSafely(Field aField) {
        return matchers.compoundMatcher().matches(aField);
    }

    @Override
    public void describeTo(Description description) {
        this.matchers.compoundMatcher().describeTo(description);
    }

    @Override
    protected void describeMismatchSafely(Field item, Description mismatchDescription) {
        this.matchers.compoundMatcher().describeMismatch(item, mismatchDescription);
    }
}
