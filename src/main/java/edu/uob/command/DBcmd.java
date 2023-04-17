package edu.uob.command;

import edu.uob.DBExceptions.DBException.Entity;
import edu.uob.DBExceptions.ParseException;
import edu.uob.DBExceptions.ParseException.*;

import edu.uob.DBServer;
import edu.uob.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public abstract class DBcmd {
    protected Conditions conditions = new Conditions();
    protected List<String[]> conds = new ArrayList<>();
    protected List<String[]> wholeConds = new ArrayList<>();

    protected List<String> tableNames = new ArrayList<>();
    protected String dbName;
    protected CMDType commandType;
    protected List<String> attributeList = new ArrayList<>();

    public abstract String query(DBServer server) throws Exception;

    public String parseTableName(String tableName) throws ParseException {
        if (KeyWords.isKeyWord(tableName)) {
            throw new NamingException(tableName, Entity.TABLE);
        }
        tableName = tableName.toLowerCase();
        return parsePlainText(tableName);
    }

    public String parseDatabaseName(String databaseName) throws ParseException {
        if (KeyWords.isKeyWord(databaseName)) {
            throw new NamingException(databaseName, Entity.DATABASE);
        }
        databaseName = databaseName.toLowerCase();
        return parsePlainText(databaseName);
    }

    public String parsePlainText(String text) throws ParseException {
        if (text.length() < 1) {
            throw new TokenMissingException("[PLAIN TEXT]");
        }
        for (int i=0; i < text.length(); i++) {
            if (!(parseLetter(text.charAt(i)) || parseDigit(text.charAt(i)))) {
                throw new InvalidTokenException(String.format("Expecting [a-zA-Z] | [0-9] at #%d in plain text [%s]", i+1, text));
            }
        }
        return text;
    }

    public String parseValue(String value) throws ParseException {
        if (value.length() < 1) {
            throw new TokenMissingException("[VALUE]");
        }
        if (value.charAt(0) == '\'') {
            String[] fragments = value.split("'");
            if (fragments.length < 2) { return " "; }
            return parseStringLiteral(fragments[1]);
        }
        if (parseBooleanLiteral(value)) { return value; }
        if (parseFloatLiteral(value)) { return value; }
        if (parseIntegerLiteral(value)) { return value; }
        if (value.equalsIgnoreCase("NULL")) { return value; }
        throw new TokenMissingException("[StringLiteral] | [BooleanLiteral] | [FloatLiteral] | [IntegerLiteral]");
    }

    public String parseStringLiteral(String str) throws ParseException {
        for (int i=0; i < str.length(); i++) {
            parseCharLiteral(str.charAt(i));
        }
        return str;
    }

    public boolean parseBooleanLiteral(String bool) {
        return bool.equals("TRUE") || bool.equals("FALSE");
    }

    public boolean parseFloatLiteral(String floatNum) {
        try {
            Double.valueOf(floatNum);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public boolean parseIntegerLiteral(String intNum) {
        try {
            Integer.valueOf(intNum);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public void parseCharLiteral(char ch) throws ParseException {
        if (!(parseSpace(ch) || parseLetter(ch) || parseSymbol(ch) || parseDigit(ch))) {
            throw new InvalidTokenException(String.format("[%s] not expected in [%s]", ch, "CharLiteral"));
        }
    }

    public boolean parseSpace(char ch) { return Character.isSpaceChar(ch); }

    public boolean parseLetter(char ch) {
        return Character.isUpperCase(ch) || Character.isLowerCase(ch);
    }

    public boolean parseSymbol(char ch) {
        String symbols = "!#$%&()*+,-./:;>=<?@[\\]^_`{}~";
        return symbols.contains(String.valueOf(ch));
    }

    public boolean parseDigit(char ch) { return Character.isDigit(ch); }

    public void parseAttributeList(Tokenizer tokenizer) throws ParseException {
        this.attributeList.add(parseAttributeName(tokenizer));
        if (tokenizer.peekNextToken().equals(",")) {
            tokenizer.incrementTokenIdx();
            parseAttributeList(tokenizer);
        }
    }

    public String parseAttributeName(Tokenizer tokenizer) throws ParseException {
        String attributeName = tokenizer.nextToken();
        if (KeyWords.isKeyWord(attributeName)) {
            throw new NamingException(attributeName, Entity.ATTRIBUTE);
        }
        if (attributeName.contains(".")) {
            parseTableName(attributeName.split("\\.")[0]);
            return parsePlainText(attributeName.split("\\.")[1]);
        } else {
            return parsePlainText(attributeName);
        }
    }

    public void parseCondition(Tokenizer tokenizer, int parenNum) throws ParseException {
        while (tokenizer.peekNextToken().equals("(")) {
            parenNum++;
            tokenizer.incrementTokenIdx();
        }
        String[] cond = {"", "", "",""}; // {"1","age",">","20"}, first number indicates priority level
        cond[0] = String.valueOf(parenNum);
        cond[1] = parseAttributeName(tokenizer);
        cond[2] = parseComparator(tokenizer.nextToken());
        cond[3] = parseValue(tokenizer.nextToken());
        this.conds.add(cond);
        this.wholeConds.add(cond);
        while (tokenizer.peekNextToken().equals(")")) {
            if (parenNum <= 0) { throw new TokenMissingException("("); }
            parenNum--;
            tokenizer.incrementTokenIdx();
        }
        if (parseBoolOperator(tokenizer.peekNextToken())) {
            String[] boolCond = {String.valueOf(parenNum), tokenizer.nextToken()};
            this.wholeConds.add(boolCond);
            parseCondition(tokenizer, parenNum);
        }
    }

    public boolean parseBoolOperator(String boolStr) {
        return boolStr.equals("AND") || boolStr.equals("OR");
    }

    public String parseComparator(String compStr) throws ParseException {
        String[] comparators = {"==",">","<",">=","<=","!=","LIKE"};
        for (String comp: comparators) {
            if (compStr.equals(comp)) return compStr;
        }
        throw new TokenMissingException("Comparator");
    }

    public void endOfCommandCheck(Tokenizer tokenizer) throws ParseException {
        if (tokenizer.getCurrentTokenIdx() != tokenizer.getTokensSize() - 1) {
            throw new TokenAfterSemicolon();
        }
    }
}
