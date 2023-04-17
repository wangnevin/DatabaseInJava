package edu.uob.command;

import edu.uob.DBExceptions.DBException;
import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.ParseException.*;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;
import edu.uob.DBExceptions.DBException.Entity;
import edu.uob.DBServer;
import edu.uob.Tokenizer;

public class CreateCMD extends DBcmd {
    public CreateCMD(Tokenizer tokenizer) throws ParseException {
        if (tokenizer.peekNextToken().equals("DATABASE")) {
            this.commandType = CMDType.CREATEDATABASE;
            tokenizer.incrementTokenIdx();
            parseCreateDatabase(tokenizer);
        } else if (tokenizer.peekNextToken().equals("TABLE")) {
            this.commandType = CMDType.CREATETABLE;
            tokenizer.incrementTokenIdx();
            parseCreateTable(tokenizer);
        } else {
            throw new TokenMissingException("DATABASE | TABLE");
        }
        if (!tokenizer.nextToken().equals(";")) {
            throw new TokenMissingException(";");
        }
        endOfCommandCheck(tokenizer);
    }

    private void parseCreateDatabase(Tokenizer tokenizer) throws ParseException {
        this.dbName = parseDatabaseName(tokenizer.nextToken());
    }

    private void parseCreateTable(Tokenizer tokenizer) throws ParseException {
        this.tableNames.add(parseTableName(tokenizer.nextToken()));
        if (tokenizer.peekNextToken().equals("(")) {
            tokenizer.incrementTokenIdx();
            parseAttributeList(tokenizer);
            if (!tokenizer.nextToken().equals(")")) {
                throw new TokenMissingException(")");
            }
        }
    }
    @Override
    public String query(DBServer server) throws QueryException {
        if (this.commandType == CMDType.CREATEDATABASE) {
            return queryCreateDatabase(server);
        }
        if (this.commandType == CMDType.CREATETABLE) {
            return queryCreateTable(server);
        }
        throw new NotDefinedException("CREATE DATABASE | CREATE TABLE", CMDType.CREATE);
    }

    private String queryCreateDatabase(DBServer server) throws QueryException {
        if (this.dbName == null || this.dbName.equals("")) {
            throw new NotDefinedException(Entity.DATABASE, CMDType.CREATE);
        }
        server.manager.createDatabase(this.dbName);
        return "Database [" + this.dbName + "] created";
    }

    private String queryCreateTable(DBServer server) throws QueryException {
        if (this.tableNames == null || this.tableNames.size() == 0) {
            throw new NotDefinedException(Entity.TABLE, CMDType.CREATE);
        }
        if (this.attributeList.contains("id")) {
            throw new QueryException("[id] is not allowed to be created manually");
        }
        server.manager.getCurrentDB().createTable(this.tableNames.get(0), this.attributeList);
        return "Table [" + this.tableNames.get(0) + "] created";
    }
}
