package org.codingmatters.value.objects.php.phpmodel;

import java.util.ArrayList;
import java.util.List;

public class PhpMethod {

    private String name;
    private List<PhpParameter> parameters;
    private List<String> instructions;

    public PhpMethod( String name ) {
        this.name = name;
        this.parameters = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }

    public void addParameters( PhpParameter parameter ) {
        this.parameters.add( parameter );
    }

    public void addInstruction( String instruction ) {
        this.instructions.add( instruction );
    }

    public String name() {
        return this.name;
    }

    public List<PhpParameter> parameters() {
        return this.parameters;
    }

    public List<String> instructions() {
        return instructions;
    }
}
