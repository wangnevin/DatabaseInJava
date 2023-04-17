package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.*;

public class JoinTests {
    TestGenerator tests;
    DBServer server;

    List<String> Name = new ArrayList<>();
    List<String> Code = new ArrayList<>();
    List<Integer> Age = new ArrayList<>();
    List<Integer> Unit = new ArrayList<>();
    List<String> Lecturer = new ArrayList<>();
    List<String> UnitName = new ArrayList<>();
    @BeforeEach
    public void Setup() {
        this.tests = new TestGenerator();
        this.server = new DBServer();
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        sendCommandToServer("USE School;");
        sendCommandToServer("CREATE TABLE students (Code, Name, Age, Unit);");
        String query = "INSERT INTO students VALUES ('%s', '%s', %s, %s);";
        int age = 18;
        int unit = 1;
        for (int idx=0; idx < 20; idx++) {
            String code = tests.validPlainText(10);
            String name = tests.validPlainText(10);
            sendCommandToServer(String.format(query, code, name, age, unit));
            Code.add(code);
            Name.add(name);
            Age.add(age);
            Unit.add(unit);
            age++; unit++;
        }
        sendCommandToServer("UPDATE students SET unit = 5 WHERE id >= 4 and id <= 7;");
        sendCommandToServer("UPDATE students SET unit = 13 WHERE id >= 16 and id <= 18;");
        sendCommandToServer("create table Units (Name, Lecturer);");
        for (int i=0; i < 20; i++) {
            query = "insert into units values ('%s', '%s');";
            String lecturer = tests.validPlainText(10);
            String unitName = tests.validPlainText(10);
            this.Lecturer.add(lecturer);
            this.UnitName.add(unitName);
            sendCommandToServer(String.format(query, unitName, lecturer));
        }
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void basicJoinTest() {
        String failedMessage = "The column [%s] should contain [%s]";
        String query = "JOIN students AND units ON units.id AND students.unit;";
        String response = sendCommandToServer(query);
        String[] lines = response.split("\n");
        for (int i=2; i < lines.length; i++) {
            assertTrue(lines[i].contains(this.Name.get(i-2)), String.format(failedMessage, "Name", this.Name.get(i-2)));
            assertTrue(lines[i].contains(this.Code.get(i-2)), String.format(failedMessage, "Code", this.Code.get(i-2)));
        }
        for (int i=5; i < 9; i++) {
            assertTrue(lines[i].contains(this.UnitName.get(4)), String.format(failedMessage, "UnitName", this.UnitName.get(i-2)));
            assertTrue(lines[i].contains(this.Lecturer.get(4)), String.format(failedMessage, "Lecturer", this.Lecturer.get(i-2)));
        }
        query = "JOIN students AND units ON unit AND id;";
        response = sendCommandToServer(query);
        lines = response.split("\n");
        for (int i=2; i < lines.length; i++) {
            assertTrue(lines[i].contains(this.Name.get(i-2)), String.format(failedMessage, "Name", this.Name.get(i-2)));
            assertTrue(lines[i].contains(this.Code.get(i-2)), String.format(failedMessage, "Code", this.Code.get(i-2)));
        }
        for (int i=5; i < 9; i++) {
            assertTrue(lines[i].contains(this.UnitName.get(4)), String.format(failedMessage, "UnitName", this.UnitName.get(i-2)));
            assertTrue(lines[i].contains(this.Lecturer.get(4)), String.format(failedMessage, "Lecturer", this.Lecturer.get(i-2)));
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void joinTest() {
        sendCommandToServer("drop database schools;");
        sendCommandToServer("create database schools;");
        sendCommandToServer("use schools;");
        sendCommandToServer("create table students (Name, Age, Unit);");
        sendCommandToServer("insert into students values ('Nevin', 27, 'Computer Science');");
        sendCommandToServer("insert into students values ('Micheal', 26, 'Law');");
        sendCommandToServer("insert into students values ('Jake', 26, 'Physics');");
        sendCommandToServer("insert into students values ('Bob', 24, 'Chemistry');");
        sendCommandToServer("insert into students values ('Chris', 25, 'Computer Science');");
        sendCommandToServer("insert into students values ('Mike', 28, 'Law');");
        sendCommandToServer("insert into students values ('Norman', 23, 'Chemistry');");
        sendCommandToServer("insert into students values ('Adam', 29, NULL);");
        sendCommandToServer("create table units (Name, Mark);");
        sendCommandToServer("insert into units values ('Computer Science', 78);");
        sendCommandToServer("insert into units values ('Chemistry', 67);");
        sendCommandToServer("insert into units values ('Physics', 72);");
        sendCommandToServer("insert into units values ('Law', 66);");

        String failedMessage = "Response should contain [%s]";
        String failedMessage1 = "Response should not contain [%s]";
        String query = "join students and units on unit and name;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertFalse(response.contains("Adam"), String.format(failedMessage1, "Adam"));
        assertFalse(response.contains("NULL"), String.format(failedMessage1, "NULL"));
        assertTrue(response.contains("1\tNevin\t27\t78"), String.format(failedMessage, "1\tNevin\t27\t78"));
        assertTrue(response.contains("2\tMicheal\t26\t66"), String.format(failedMessage, "2\tMicheal\t26\t66"));
        assertTrue(response.contains("3\tJake\t26\t72"), String.format(failedMessage, "3\tJake\t26\t72"));
        assertTrue(response.contains("4\tBob\t24\t67"), String.format(failedMessage, "4\tBob\t24\t67"));
        assertTrue(response.contains("5\tChris\t25\t78"), String.format(failedMessage, "5\tChris\t25\t78"));
        assertTrue(response.contains("6\tMike\t28\t66"), String.format(failedMessage, "6\tMike\t28\t66"));
        assertTrue(response.contains("7\tNorman\t23\t67"), String.format(failedMessage, "7\tNorman\t23\t67"));

        sendCommandToServer("delete from units where name == 'Computer Science';");
        response = sendCommandToServer(query);
        assertFalse(response.contains("Nevin") || response.contains("Chris"), String.format(failedMessage1, "Nevin | Chris"));
        assertTrue(response.contains("1\tMicheal\t26\t66"), String.format(failedMessage, "2\tMicheal\t26\t66"));
        assertTrue(response.contains("2\tJake\t26\t72"), String.format(failedMessage, "3\tJake\t26\t72"));
        assertTrue(response.contains("3\tBob\t24\t67"), String.format(failedMessage, "4\tBob\t24\t67"));
        assertTrue(response.contains("4\tMike\t28\t66"), String.format(failedMessage, "6\tMike\t28\t66"));
        assertTrue(response.contains("5\tNorman\t23\t67"), String.format(failedMessage, "7\tNorman\t23\t67"));

        sendCommandToServer("drop database schools;");
    }
}
