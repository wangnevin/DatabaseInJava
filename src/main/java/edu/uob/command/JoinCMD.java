package edu.uob.command;

import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;
import edu.uob.DBServer;
import edu.uob.Tokenizer;

import java.util.HashMap;

public class JoinCMD extends DBcmd {
    private HashMap<String, String> tableAttriPair = new HashMap<>();
    public JoinCMD(Tokenizer tokenizer) throws ParseException {
        this.commandType = CMDType.JOIN;
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        if (!tokenizer.nextToken().equals("AND")) {
            throw new TokenMissingException("AND");
        }
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        if (!tokenizer.nextToken().equals("ON")) {
            throw new TokenMissingException("ON");
        }
        this.attributeList.add(parseAttributeName(tokenizer));
        if (!tokenizer.nextToken().equals("AND")) {
            throw new TokenMissingException("AND");
        }
        this.attributeList.add(parseAttributeName(tokenizer));
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
        processTableAttriPair();
    }

    public String parseAttributeName(Tokenizer tokenizer) throws ParseException {
        String attributeName = tokenizer.nextToken();
        if (attributeName.contains(".")) {
            String tableName = parseTableName(attributeName.split("\\.")[0]);
            String attriName = parsePlainText(attributeName.split("\\.")[1]);
            this.tableAttriPair.put(tableName, attriName);
            return attriName;
        } else {
            return parsePlainText(attributeName);
        }
    }

    private void processTableAttriPair() throws ParseException {
        if (this.tableNames.size() != 2) {
            throw new ParseException(String.format("Expecting 2 tables, but %d was given", this.tableNames.size()));
        }
        if (this.attributeList.size() != 2) {
            throw new ParseException(String.format("Expecting 2 attributes, but %d was given", this.attributeList.size()));
        }
        if (tableAttriPair.size() == 0) { return; }
        if (this.tableAttriPair.size() > 2) {
            throw new ParseException(String.format("Expecting 2 table-attribute pairs, but %d was given", this.tableAttriPair.size()));
        }
        String attri1 = this.tableAttriPair.get(this.tableNames.get(0));
        String attri2 = this.tableAttriPair.get(this.tableNames.get(1));
        this.attributeList.set(0, attri1);
        this.attributeList.set(1, attri2);
    }
    @Override
    public String query(DBServer server) throws QueryException {
        return server.manager.getCurrentDB().joinTable(this.tableNames, this.attributeList);
    }
}
