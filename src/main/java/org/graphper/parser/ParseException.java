package org.graphper.parser;

public class ParseException extends RuntimeException {

    private String sourceName;

    public ParseException() {

        super();
    }

    public ParseException(String message) {

        super(message);
    }

    public ParseException(String message, Throwable cause) {

        super(message, cause);
    }

    public ParseException(Throwable cause) {

        super(cause);
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ", source: " + sourceName;
    }
}
