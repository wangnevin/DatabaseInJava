package edu.uob.command;

import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;
import edu.uob.DBServer;
import edu.uob.Tokenizer;

import java.util.ArrayList;

public class DropCMD extends DBcmd {
    public DropCMD(Tokenizer tokenizer) throws ParseException {
        if (tokenizer.peekNextToken().equals("DATABASE")) {
            this.commandType = CMDType.DROPDATABASE;
            tokenizer.incrementTokenIdx();
            parseDropDatabase(tokenizer);
        } else if (tokenizer.peekNextToken().equals("TABLE")) {
            this.commandType = CMDType.DROPTABLE;
            tokenizer.incrementTokenIdx();
            parseDropTable(tokenizer);
        } else {
            throw new TokenMissingException("DATABASE | TABLE");
        }
    }

    private void parseDropDatabase(Tokenizer tokenizer) throws ParseException {
        this.dbName = parseDatabaseName(tokenizer.nextToken());
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }

    private void parseDropTable(Tokenizer tokenizer) throws ParseException {
        this.tableNames = new ArrayList<>();
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }
    @Override
    public String query(DBServer server) throws QueryException {
        if (this.commandType == CMDType.DROPDATABASE) {
            return queryDropDatabase(server);
        } else if (this.commandType == CMDType.DROPTABLE) {
            return queryDropTable(server);
        } else {
            throw new NotDefinedException("DATABASE | TABLE", CMDType.DROP);
        }
    }

    private String queryDropDatabase(DBServer server) throws QueryException {
        server.manager.dropDataBase(this.dbName);
        return "Deleted database [" + this.dbName + "]";
    }

    private String queryDropTable(DBServer server) throws QueryException {
        server.manager.getCurrentDB().dropTable(this.tableNames.get(0));
        return "Deleted table [" + this.tableNames.get(0) + "]";
    }
}
