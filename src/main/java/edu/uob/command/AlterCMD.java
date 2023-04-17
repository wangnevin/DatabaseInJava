package edu.uob.command;

import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;
import edu.uob.DBExceptions.DBException.Entity;
import edu.uob.DBServer;
import edu.uob.DatabaseManager;
import edu.uob.Tokenizer;

public class AlterCMD extends DBcmd {
    private String alterType;
    public AlterCMD(Tokenizer tokenizer) throws ParseException {
        this.commandType = CMDType.ALTER;
        if (!tokenizer.nextToken().equals("TABLE")) {
            throw new TokenMissingException("TABLE");
        }
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        parseAlterationType(tokenizer);
        this.attributeList.add(parseAttributeName(tokenizer));
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }

    private void parseAlterationType(Tokenizer tokenizer) throws ParseException {
        String alterType = tokenizer.nextToken();
        if (!alterType.equals("ADD") && !alterType.equals("DROP")) {
            throw new TokenMissingException("ADD | DROP");
        }
        this.alterType = alterType;
    }
    @Override
    public String query(DBServer server) throws QueryException {
        if (this.tableNames == null || this.tableNames.size() == 0) {
            throw new NotDefinedException(Entity.TABLE, CMDType.ALTER);
        }
        if (this.attributeList == null || this.attributeList.size() == 0) {
            throw new NotDefinedException(Entity.ATTRIBUTE, CMDType.ALTER);
        }
        if (this.alterType == null) {
            throw new NotDefinedException("[ADD | DROP]", CMDType.ALTER);
        }
        if (this.alterType.equals("ADD")) {
            return queryAlterAdd(server.manager);
        } else {
            return queryAlterDrop(server.manager);
        }
    }

    private String queryAlterAdd(DatabaseManager manager) throws QueryException {
        manager.getCurrentDB().getTable(this.tableNames.get(0)).alterAdd(this.attributeList.get(0));
        return String.format("Succeeded to add [%s] to table [%s]", this.attributeList.get(0), this.tableNames.get(0));
    }

    private String queryAlterDrop(DatabaseManager manager) throws QueryException {
        if (this.attributeList.get(0).equalsIgnoreCase("id")) {
            throw new ModifyIdException();
        }
        manager.getCurrentDB().getTable(this.tableNames.get(0)).alterDrop(this.attributeList.get(0));
        return String.format("Succeeded to drop [%s] from table [%s]", this.attributeList.get(0), this.tableNames.get(0));
    }
}
