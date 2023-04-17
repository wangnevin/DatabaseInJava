package edu.uob.DBExceptions;

import edu.uob.command.CMDType;

import java.io.Serial;

public class QueryException extends DBException {
    @Serial
    private static final long serialVersionUID = 1;
    public QueryException(String message) {
        super(message);
    }
    public static class EntityNotFoundException extends QueryException {
        @Serial private static final long serialVersionUID = 1;
        public EntityNotFoundException(String name, Entity entity) {
            super(String.format("%s [%s] doesn't exist", entity.name(), name));
        }
    }

    public static class DoubleCreateException extends QueryException {
        @Serial private static final long serialVersionUID = 1;
        public DoubleCreateException(String name, Entity entity) {
            super(String.format("%s [%s] already exists", entity.name(), name));
        }
    }

    public static class NotDefinedException extends QueryException {
        @Serial private static final long serialVersionUID = 1;
        public NotDefinedException(String notDefined, CMDType command) {
            super(String.format("[%s] not defined for [%s] command", notDefined, command.name()));
        }

        public NotDefinedException(Entity entity, CMDType command) {
            super(String.format("[%s] not defined for [%s] command", entity.name(), command.name()));
        }

        public NotDefinedException(Entity entity) {
            super(String.format("[%s] not defined", entity.name()));
        }
    }

    public static class ModifyIdException extends QueryException {
        @Serial private static final long serialVersionUID = 1;
        public ModifyIdException() {
            super("[Id] is not allowed to be modified");
        }
    }
}
