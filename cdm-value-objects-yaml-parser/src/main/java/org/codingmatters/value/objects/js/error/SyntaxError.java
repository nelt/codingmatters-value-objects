package org.codingmatters.value.objects.js.error;

public class SyntaxError extends Exception {
    public SyntaxError( Exception e ) {
        super( e );
    }

    public SyntaxError
            ( String message ) {
        super( message );
    }
}
