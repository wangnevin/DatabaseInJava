package edu.uob.DBExceptions;

import edu.uob.command.CMDType;

import java.io.Serial;

public class ParseException extends DBException {
    @Serial
    private static final long serialVersionUID = 1;
    public ParseException(String message) {
        super(message);
    }

    public static class NamingException extends ParseException {
        @Serial private static final long serialVersionUID = 1;
        public NamingException(String name, Entity entity) {
            super(String.format("[%s] is invalid name for [%s]", name, entity.name()));
        }
    }

    public static class TokenMissingException extends ParseException {
        @Serial private static final long serialVersionUID = 1;
        public TokenMissingException(String token) {
            super(String.format("Expecting [%s] in command", token));
        }
    }

    public static class InvalidTokenException extends ParseException {
        @Serial private static final long serialVersionUID = 1;
        public InvalidTokenException(String message) {
            super(message);
        }
    }

    public static class TokenAfterSemicolon extends ParseException {
        @Serial private static final long serialVersionUID = 1;
        public TokenAfterSemicolon() {
            super("No tokens are allowed after [;]");
        }
    }
}

