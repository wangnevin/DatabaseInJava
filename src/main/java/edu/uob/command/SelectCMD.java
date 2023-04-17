package edu.uob.command;

import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBServer;
import edu.uob.Tokenizer;


public class SelectCMD extends DBcmd {
    public SelectCMD(Tokenizer tokenizer) throws ParseException {
        this.commandType = CMDType.SELECT;
        parseWildAttributeList(tokenizer);
        if (!tokenizer.nextToken().equals("FROM")) {
            throw new TokenMissingException("FROM");
        }
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        if (tokenizer.peekNextToken().equals("WHERE")) {
            tokenizer.incrementTokenIdx();
            parseCondition(tokenizer, 0);
            this.conditions.createPredicates(this.conds, this.wholeConds);
        }
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }

    private void parseWildAttributeList(Tokenizer tokenizer) throws ParseException {
        if (tokenizer.peekNextToken().equals("*")) {
            tokenizer.incrementTokenIdx();
            this.attributeList.add("*");
        } else {
            parseAttributeList(tokenizer);
        }
    }
    @Override
    public String query(DBServer server) throws QueryException {
        return server.manager.getCurrentDB().getTable(this.tableNames.get(0)).selectValue(this.conditions, this.attributeList);
    }
}
