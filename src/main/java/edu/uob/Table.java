package edu.uob;

import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;
import edu.uob.DBExceptions.DBException.Entity;
import edu.uob.command.Conditions;

import java.io.*;
import java.util.*;

public class Table {
    private String tableName;
    private File tableFile;
    private int nextAvailablePK = 0;
    private List<String> tableHeads = new ArrayList<>();
    private List<Map<String, String>> tableRows = new ArrayList<>();

    public Table(File tableFile) {
        try {
            this.tableFile = tableFile;
            this.tableName = tableFile.getName().split("\\.")[0];
            readTable();
            setNextAvailablePK();
        } catch (Exception e) {
            System.out.println("Failed to initialize table");
        }
    }

    public Table(List<String> heads, List<Map<String, String>> rows) {
        this.tableHeads = heads;
        this.tableRows = rows;
    }

    private void readTable() throws Exception {
        String tableStr = tableFileToString();
        tableStringToClass(tableStr);
    }

    private void setNextAvailablePK() {
        int result = 0;
        for (Map<String, String> row: this.tableRows) {
            int currentPK = Integer.parseInt(row.get("id"));
            if (currentPK > result) {
                result = currentPK;
            }
        }
        this.nextAvailablePK = result + 1;
    }

    public String getTableName() { return this.tableName; }
    public List<Map<String, String>> getTableRows() { return this.tableRows; }
    public Map<String, String> getRowByIdx(int idx) { return this.tableRows.get(idx); }

    public void insertValue(List<String> valueList) throws QueryException {
        valueList.add(0, String.valueOf(this.nextAvailablePK));
        this.nextAvailablePK++;
        if (valueList.size() != this.tableHeads.size()) {
            throw new QueryException("Number of given value doesn't match number of columns");
        }
        Map<String, String> newRow = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i=0; i < valueList.size(); i++) {
            newRow.put(this.tableHeads.get(i), valueList.get(i));
        }
        this.tableRows.add(newRow);
        tableClassToFile();
    }

    public String selectValue(Conditions conditions, List<String> attriList) throws QueryException {
        for (String[] cond: conditions.getConds()) {
            if (!this.tableHeads.contains(getOriginHead(cond[1]))) {
                throw new EntityNotFoundException(cond[1], Entity.ATTRIBUTE);
            }
        }
        for (String attriName: attriList) {
            if (!attriName.equals("*") && !this.tableHeads.contains(getOriginHead(attriName))) {
                throw new EntityNotFoundException(attriName, Entity.ATTRIBUTE);
            }
        }
        List<Map<String, String>> resultRows;
        resultRows = rowFilter(this.tableRows, conditions);
        resultRows = colFilter(resultRows, attriList);
        return resultToString(attriList, resultRows);
    }

    public void updateValue(Conditions conditions, List<String[]> nameValueList) throws QueryException {
        for (String[] cond: conditions.getConds()) {
            if (!this.tableHeads.contains(getOriginHead(cond[1]))) {
                throw new EntityNotFoundException(cond[1], Entity.ATTRIBUTE);
            }
        }
        List<String> resultRowsId;
        resultRowsId = this.tableRows.stream().filter(conditions.getPredicate()).map(x -> x.get("id")).toList();
        for (Map<String, String> row: this.tableRows) {
            if (resultRowsId.contains(row.get("id"))) {
                updateRow(row, nameValueList);
            }
        }
        tableClassToFile();
    }

    public void alterAdd(String attributeName) throws QueryException {
        if (tableHeads.contains(getOriginHead(attributeName))) {
            throw new DoubleCreateException(attributeName, Entity.ATTRIBUTE);
        }
        this.tableHeads.add(attributeName);
        for (Map<String, String> row: this.tableRows) {
            row.put(attributeName, "NULL");
        }
        tableClassToFile();
    }

    public void alterDrop(String attributeName) throws QueryException {
        if (!this.tableHeads.contains(getOriginHead(attributeName))) {
            throw new EntityNotFoundException(attributeName, Entity.ATTRIBUTE);
        }
        this.tableHeads.remove(getOriginHead(attributeName));
        for (Map<String, String> row: this.tableRows) {
            row.remove(attributeName);
        }
        tableClassToFile();
    }

    public void deleteRows(Conditions conditions) throws QueryException {
        for (String[] cond: conditions.getConds()) {
            if (!this.tableHeads.contains(getOriginHead(cond[1]))) {
                throw new EntityNotFoundException(cond[1], Entity.ATTRIBUTE);
            }
        }
        List<String> resultRowsId = this.tableRows.stream().filter(conditions.getPredicate()).map(x -> x.get("id")).toList();
        this.tableRows.removeIf(row -> resultRowsId.contains(row.get("id")));
        tableClassToFile();
    }

    private List<Map<String, String>> rowFilter(List<Map<String, String>> stream, Conditions conditions) {
        if (conditions.hasCondition()) {
            stream = stream.stream().filter(conditions.getPredicate()).toList();
        }
        return stream;
    }
    private List<Map<String, String>> colFilter(List<Map<String, String>> stream, List<String> attriList) {
        if (!(attriList.size() == 1 && attriList.get(0).equals("*"))) {
            stream = attributeFilter(stream, attriList);
        }
        return stream;
    }

    private List<Map<String, String>> attributeFilter(List<Map<String, String>> rows, List<String> attriList) {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, String> row: rows) {
            Map<String, String> newRow = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (String attri: attriList) {
                newRow.put(attri, row.get(attri));
            }
            result.add(newRow);
        }
        return result;
    }

    private void updateRow(Map<String, String> row, List<String[]> nameValueList) throws QueryException {
        for (String[] nameValue: nameValueList) {
            if (!row.containsKey(nameValue[0])) {
                throw new EntityNotFoundException(nameValue[0], Entity.ATTRIBUTE);
            }
            row.put(nameValue[0], nameValue[1]);
        }
    }

    public String resultToString(List<String> attriList, List<Map<String, String>> rows) {
        StringBuilder result = new StringBuilder();
        if (attriList.size() == 1 && attriList.get(0).equals("*")) { attriList = this.tableHeads; }
        for (String attri: attriList) {
            result.append(getOriginHead(attri)).append("\t");
        }
        result.append("\n");
        for (Map<String, String> row: rows) {
            result.append(rowToString(row, attriList));
        }
        return result.toString();
    }

    private String tableFileToString() throws IOException {
        if (!this.tableFile.exists()) {
            throw new FileNotFoundException("Table [" + this.tableName + "] doesn't exists");
        }
        FileReader reader = new FileReader(this.tableFile);
        BufferedReader buffReader = new BufferedReader(reader);
        String result = "";
        String newLine = buffReader.readLine();
        while (newLine != null) {
            result += newLine;
            newLine = buffReader.readLine();
            if (newLine != null) {
                result += "\n";
            }
        }
        buffReader.close();
        return result;
    }

    public void tableStringToClass(String tabStr) throws Exception {
        readTableHead(tabStr);
        readTableRows(tabStr);
    }

    public String tableClassToString() {
        String result = "";
        result += headToString();
        for (Map<String, String> tableRow : this.tableRows) {
            result += rowToString(tableRow, this.tableHeads);
        }
        return result;
    }

    public void tableClassToFile() throws QueryException {
        try {
            FileWriter writer = new FileWriter(this.tableFile);
            for (int i=0; i < this.tableHeads.size(); i++) {
                writer.write(this.tableHeads.get(i));
                if (i + 1 < this.tableHeads.size()) { writer.write("\t"); }
            }
            writer.write("\n");
            for (Map<String, String> row: this.tableRows) {
                for (int i=0; i < row.size(); i++) {
                    writer.write(row.get(this.tableHeads.get(i)));
                    if (i + 1 < this.tableHeads.size()) { writer.write("\t"); }
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new QueryException("Failed to write to file for table");
        }
    }

    private void readTableHead(String tableStr) throws Exception {
        String[] tableList = tableStr.split("\n");
        if (tableList.length < 1) {
            throw new Exception("No content in .tab file");
        }
        String[] tableHeads = tableList[0].split("\t");
        this.tableHeads.addAll(Arrays.asList(tableHeads));
        if (this.tableHeads.size() < 1) {
            throw new Exception("Invalid table head");
        }
    }

    private void readTableRows(String tableStr) throws Exception {
        String[] tableList = tableStr.split("\n");
        if (tableList.length < 1) {
            throw new Exception("No content in .tab file");
        }
        for (int i=1; i<tableList.length; i++) {
            String[] columns = tableList[i].split("\t");
            Map<String, String> columnValues = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (int j=0; j<this.tableHeads.size(); j++) {
                columnValues.put(this.tableHeads.get(j), columns[j]);
            }
            this.tableRows.add(columnValues);
        }
    }

    public List<String> getTableHead() { return this.tableHeads; }

    private String rowToString(Map<String, String> row, List<String> attri) {
        String result = "";
        for (int i=0; i < row.size(); i++) {
            result += row.get(attri.get(i));
            if (i + 1 < row.size()) {
                result += '\t';
            }
        }
        return result + '\n';
    }

    private String headToString() {
        String result = "";
        for (int i=0; i < this.tableHeads.size(); i++) {
            result += this.tableHeads.get(i);
            if (i + 1 < this.tableHeads.size()) {
                result += '\t';
            }
        }
        return result + '\n';
    }

    private String getOriginHead(String attribute) {
        for (String head: this.tableHeads) {
            if (head.equalsIgnoreCase(attribute)) {
                return head;
            }
        }
        return attribute;
    }
}
