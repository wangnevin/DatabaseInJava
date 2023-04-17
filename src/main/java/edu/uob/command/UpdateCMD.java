package edu.uob.command;

import edu.uob.DBExceptions.DBException.*;
import edu.uob.DBServer;
import edu.uob.Tokenizer;
import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;

import java.util.ArrayList;
import java.util.List;

public class UpdateCMD extends DBcmd {
    private List<String[]> nameValueList = new ArrayList<>();
    public UpdateCMD(Tokenizer tokenizer) throws ParseException {
        this.commandType = CMDType.UPDATE;
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        if (!tokenizer.nextToken().equals("SET")) {
            throw new TokenMissingException("SET");
        }
        parseNameValueList(tokenizer);
        if (!tokenizer.nextToken().equals("WHERE")) {
            throw new TokenMissingException("WHERE");
        }
        parseCondition(tokenizer, 0);
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }

    private void parseNameValueList(Tokenizer tokenizer) throws ParseException {
        this.nameValueList.add(parseNameValuePair(tokenizer));
        if (tokenizer.peekNextToken().equals(",")) {
            tokenizer.incrementTokenIdx();
            this.nameValueList.add(parseNameValuePair(tokenizer));
        }
    }

    private String[] parseNameValuePair(Tokenizer tokenizer) throws ParseException {
        String name = parseAttributeName(tokenizer);
        if (!tokenizer.nextToken().equals("=")) {
            throw new TokenMissingException("=");
        }
        String value = parseValue(tokenizer.nextToken());
        return new String[]{name, value};
    }

    @Override
    public String query(DBServer server) throws QueryException {
        this.conditions.createPredicates(this.conds, this.wholeConds);
        if (this.tableNames == null || this.tableNames.size() == 0){
            throw new NotDefinedException(Entity.TABLE, CMDType.UPDATE);
        }
        if (this.nameValueList == null || this.nameValueList.size() == 0) {
            throw new NotDefinedException("NameValueList", CMDType.UPDATE);
        }
        for(String[] nameValue: this.nameValueList) {
            if (nameValue[0].equalsIgnoreCase("id")) {
                throw new ModifyIdException();
            }
        }
        server.manager.getCurrentDB().getTable(this.tableNames.get(0)).updateValue(this.conditions, this.nameValueList);
        return String.format("Succeeded in updating value for table [%s]", this.tableNames.get(0));
    }
}
