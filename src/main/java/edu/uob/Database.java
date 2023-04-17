package edu.uob;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import edu.uob.DBExceptions.QueryException.*;
import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.DBException.Entity;

public class Database {
    private File databaseFile;
    private Map<String, File> tablesFile = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public Database(File databaseFile) {
        try {
            this.databaseFile = databaseFile;
            readTableList();
        } catch (Exception e) {
            System.out.println("Failed to initialize database");
        }
    }

    public void createTable(String tableName, List<String> attributeList) throws QueryException {
        if (this.tablesFile.containsKey(tableName)) {
            throw new DoubleCreateException(tableName, Entity.TABLE);
        }
        File newTable = new File(this.databaseFile, tableName+".tab");
        createTable(newTable, attributeList);
    }

    public void dropTable(String tableName) throws QueryException {
        if (!this.tablesFile.containsKey(tableName)) {
            throw new EntityNotFoundException(tableName, Entity.TABLE);
        }
        if (!this.tablesFile.get(tableName).exists() || !this.tablesFile.get(tableName).isFile()) {
            throw new EntityNotFoundException(tableName, Entity.TABLE);
        }
        deleteTable(this.tablesFile.get(tableName));
    }

    public String joinTable(List<String> tableNames, List<String> attriNames) throws QueryException {
        if (tableNames.size() != 2) {
            throw new QueryException(String.format("Expecting 2 tables, but %d was given", tableNames.size()));
        }
        for (String tableName: tableNames) {
            if (!this.tablesFile.containsKey(tableName)) {
                throw new EntityNotFoundException(tableName, Entity.TABLE);
            }
        }
        int PK = 1;
        Table table0 = new Table(this.tablesFile.get(tableNames.get(0)));
        Table table1 = new Table(this.tablesFile.get(tableNames.get(1)));
        List<String> resultHeads = concatHead(table0, table1, attriNames);
        List<Map<String, String>> resultRows = new ArrayList<>();
        for (int i=0; i < table0.getTableRows().size(); i++) {
            String target = table0.getRowByIdx(i).get(attriNames.get(0));
            for (int j=0; j < table1.getTableRows().size(); j++) {
                if (table1.getRowByIdx(j).get(attriNames.get(1)).equals(target)) {
                    resultRows.add(concatRow(table0, i, table1, j, PK, attriNames));
                    PK++;
                }
            }
        }
        Table joinedTable = new Table(resultHeads, resultRows);
        return joinedTable.tableClassToString();
    }
    private List<String> concatHead(Table table0, Table table1, List<String> attriNames) {
        List<String> result = new ArrayList<>();
        result.add("id");
        addHeadOf(table0, attriNames.get(0), result);
        addHeadOf(table1, attriNames.get(1), result);
        return result;
    }

    private Map<String, String> concatRow(Table table0, int rowIdx0, Table table1, int rowIdx1, int PK, List<String> attriNames) {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        result.put("id", String.valueOf(PK));
        addRowOf(table0, rowIdx0, attriNames.get(0), result);
        addRowOf(table1, rowIdx1, attriNames.get(1), result);
        return result;
    }

    private void addHeadOf(Table table, String attriName, List<String> result) {
        for (String head: table.getTableHead()) {
            if (head.equals("id")) { continue; }
            if (head.equalsIgnoreCase(attriName)) { continue; }
            result.add(table.getTableName() + "." + head);
        }
    }

    private void addRowOf(Table table, int rowIdx, String attriName, Map<String, String> result) {
        for (String head: table.getTableHead()) {
            if (head.equals("id")) { continue; }
            if (head.equalsIgnoreCase(attriName)) { continue; }
            String newAttriName = table.getTableName() + "." + head;
            String value = table.getRowByIdx(rowIdx).get(head);
            result.put(newAttriName, value);
        }
    }

    public Table getTable(String tableName) throws QueryException {
        if (!this.tablesFile.containsKey(tableName)) {
            throw new EntityNotFoundException(tableName, Entity.TABLE);
        }
        if (!this.tablesFile.get(tableName).exists()) {
            throw new EntityNotFoundException(tableName, Entity.TABLE);
        }
        return new Table(this.tablesFile.get(tableName));
    }

    private void readTableList() {
        File[] tables = this.databaseFile.listFiles();
        if (tables == null) return;
        for (File table: tables) {
            if (table.isFile()) {
                this.tablesFile.put(table.getName().split("\\.")[0], table);
            }
        }
    }

    private void createTable(File tableFile, List<String> attributeList) throws QueryException {
        String tableName = tableFile.getName().split("\\.")[0];
        try {
            if (!tableFile.createNewFile()) { throw new IOException(); }
            attributeList.add(0, "id");
            FileWriter writer = new FileWriter(tableFile);
            for (int i=0; i < attributeList.size(); i++) {
                writer.write(attributeList.get(i));
                if (i + 1 < attributeList.size()) {
                    writer.write("\t");
                }
            }
            writer.flush();
            writer.close();
            this.tablesFile.put(tableName, tableFile);
        } catch (IOException ioe) {
            throw new QueryException(String.format("Failed to create file for table [%s]", tableName));
        }
    }

    private void deleteTable(File tableFile) throws QueryException {
        String tableName = tableFile.getName().split("\\.")[0];
        if (!tableFile.delete()) {
            throw new QueryException(String.format("Failed to delete file for table [%s]", tableName));
        }
        this.tablesFile.remove(tableName);
    }
}
