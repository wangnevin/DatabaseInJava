package edu.uob.command;

import edu.uob.DBExceptions.DBException.Entity;
import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;
import edu.uob.DBServer;
import edu.uob.Tokenizer;

public class DeleteCMD extends DBcmd {
    public DeleteCMD(Tokenizer tokenizer) throws ParseException {
        this.commandType = CMDType.DELETE;
        if (!tokenizer.nextToken().equals("FROM")) {
            throw new TokenMissingException("FROM");
        }
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        if (!tokenizer.nextToken().equals("WHERE")) {
            throw new TokenMissingException("WHERE");
        }
        parseCondition(tokenizer, 0);
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }
    @Override
    public String query(DBServer server) throws QueryException {
        if (this.tableNames == null || this.tableNames.size() == 0) {
            throw new NotDefinedException(Entity.TABLE, CMDType.DELETE);
        }
        this.conditions.createPredicates(this.conds, this.wholeConds);
        if (!this.conditions.hasCondition()) {
            throw new NotDefinedException("Conditions", CMDType.DELETE);
        }
        server.manager.getCurrentDB().getTable(this.tableNames.get(0)).deleteRows(this.conditions);
        return "Succeeded in deleting rows in " + this.tableNames.get(0);
    }
}
