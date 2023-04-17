package edu.uob.command;

import edu.uob.DBServer;
import edu.uob.Tokenizer;
import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;

import java.util.ArrayList;
import java.util.List;

public class InsertCMD extends DBcmd {
    private List<String> valueList = new ArrayList<>();
    public InsertCMD(Tokenizer tokenizer) throws ParseException {
        this.tableNames = new ArrayList<>();
        if (!tokenizer.nextToken().equals("INTO")) {
            throw new TokenMissingException("INTO");
        }
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        if (!tokenizer.nextToken().equals("VALUES")) {
            throw new TokenMissingException("VALUES");
        }
        if (!tokenizer.nextToken().equals("(")) {
            throw new TokenMissingException("(");
        }
        parseValueList(tokenizer);
        if (!tokenizer.nextToken().equals(")")) {
            throw new TokenMissingException(")");
        }
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }

    private void parseValueList(Tokenizer tokenizer) throws ParseException {
        this.valueList.add(parseValue(tokenizer.nextToken()));
        if (tokenizer.peekNextToken().equals(",")) {
            tokenizer.incrementTokenIdx();
            parseValueList(tokenizer);
        }
    }
    @Override
    public String query(DBServer server) throws QueryException {
        server.manager.getCurrentDB().getTable(this.tableNames.get(0)).insertValue(this.valueList);
        return "Successfully insert values into table [" + this.tableNames.get(0) + "]";
    }
}
