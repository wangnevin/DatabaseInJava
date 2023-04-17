package edu.uob.DBExceptions;

import java.io.Serial;

public class DBException extends Exception {
    @Serial
    private static final long serialVersionUID = 1;
    public enum Entity { DATABASE, TABLE, ATTRIBUTE }
    public DBException(String message) {
        super(message);
    }
}
