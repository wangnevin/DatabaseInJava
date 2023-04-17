package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlterTests {
    TestGenerator tests;
    DBServer server;

    List<String> Name = new ArrayList<>();
    List<String> Code = new ArrayList<>();
    List<Integer> Age = new ArrayList<>();
    List<Integer> Unit = new ArrayList<>();
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
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void basicAlterADDTest() {
        String query = "ALTER TABLE students ADD class;";
        String response = sendCommandToServer(query);
        response = sendCommandToServer("SELECT class FROM students;");
        assertTrue(response.contains("[OK]"), String.format("[%s] should be valid since column Class has been created", query));
        response = sendCommandToServer("ALTER TABLE students ADD Class;");
        assertTrue(response.contains("[ERROR]"), "This should be invalid since column Class already exists");
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void basicAlterDROPTest() {
        String query = "ALTER TABLE students DROP name;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format("[%s] should be valid", query));
        response = sendCommandToServer("SELECT name FROM students;");
        assertTrue(response.contains("[ERROR]"), "This should be invalid since column Name has already been deleted");
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void multipleAlterDropTest() {
        String failedMessage = "Only one column is allowed to be dropped";
        String query = "ALTER TABLE students DROP name, age;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("SELECT name, age FROM students;");
        assertTrue(response.contains("[OK]") && response.contains("Age") && response.contains("Name"),
                "Result should contain [Age] and [Name] column");
        query = "ALTER TABLE students DROP students.name, age;";
        response = sendCommandToServer(query);
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("SELECT name, age FROM students;");
        assertTrue(response.contains("[OK]") && response.contains("Age") && response.contains("Name"),
                "Result should contain [Age] and [Name] column");
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void alterAddIdTest() {
        String failedMessage = "[id] column is not allowed to be added by ALTER ADD command";
        String response = sendCommandToServer("ALTER TABLE students ADD id;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("ALTER TABLE students ADD Id;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("ALTER TABLE students ADD iD;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("ALTER TABLE students ADD ID;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void alterDropIdTest() {
        String failedMessage = "[id] column is not allowed to be dropped by ALTER ADD command";
        String response = sendCommandToServer("ALTER TABLE students DROP id;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("ALTER TABLE students DROP Id;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("ALTER TABLE students DROP iD;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("ALTER TABLE students DROP ID;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void alterAttributeNameTest() {
        String failedMessage = "[%s] should be a valid query";
        String query = "ALTER TABLE students ADD students.Weight;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), failedMessage);
        response = sendCommandToServer("SELECT students.weight FROM students;");
        assertTrue(response.contains("[OK]") && response.contains("Weight") && response.contains("NULL"),
                "Result should contain Weight column and NULL as value");

        query = "ALTER TABLE students DROP students.weight;";
        response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), failedMessage);
        response = sendCommandToServer("Select weight from students;");
        assertTrue(response.contains("[ERROR]"));
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void alterEmptyTableTest() {
        sendCommandToServer("create table units;");
        sendCommandToServer("alter table units add Code;");
        sendCommandToServer("alter table units add Name;");
        sendCommandToServer("alter table units add Lecturer;");
        String response = sendCommandToServer("SELECT id, code, name, lectureR FROM units;");
        assertTrue(response.contains("[OK]") && response.contains("id")
                && response.contains("Code") && response.contains("Lecturer") && response.contains("Name"),
                "Result should contain columns for id, Code, Name and Lecturer");
        sendCommandToServer("DROP DATABASE School;");
    }
}
