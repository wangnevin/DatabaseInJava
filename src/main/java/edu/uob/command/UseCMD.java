package edu.uob.command;

import edu.uob.DBServer;
import edu.uob.Tokenizer;
import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;

public class UseCMD extends DBcmd {
    public UseCMD(Tokenizer tokenizer) throws ParseException {
        this.commandType = CMDType.USE;
        this.dbName = parseDatabaseName(tokenizer.nextToken());
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }

    @Override
    public String query(DBServer server) throws QueryException {
        server.manager.switchToDB(this.dbName);
        return "Switched to database [" + this.dbName + "]";
    }
}
