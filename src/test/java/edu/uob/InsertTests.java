package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class InsertTests {
    TestGenerator tests;
    DBServer server;
    @BeforeEach
    public void Setup() {
        this.tests = new TestGenerator();
        this.server = new DBServer();
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        sendCommandToServer("USE School;");
        sendCommandToServer("CREATE TABLE students (Code, Name, Age, Course);");
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void parseRandomSpaceInsertTest() {
        String failedMessage = "#%d/%d [%s] should be a valid query, but parser judged it to be invalid";
        String query = "Insert into students Values ('ab123456', 'Micheal Wang', 24, 'Computer Science [conversion]');";
        for (int i=0; i < 100; i++) {
            query = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(query);
            assertTrue(response.contains("[OK]"), String.format(failedMessage, i+1, 100, query));
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void basicInsertSelectTest() {
        String failedMessage = "[%s] should be valid, but failed to execute";
        String query = "INsert into students values ('ab123456', 'Micheal Wang', 24, 'Computer Science [conversion]');";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format(failedMessage, query));
        for (int i=0; i < 100; i++) {
            String name1 = tests.randomCase("code");
            String name2 = tests.randomCase("age");
            String name3 = tests.randomCase("course");
            query = "Select %s, %s, %s from Students;";
            response = sendCommandToServer(String.format(query, name1, name2, name3));
            assertTrue(response.contains("[OK]"), String.format(failedMessage, query));
        }
        for (int i=0; i < 100; i++) {
            String name1 = tests.randomCase("code");
            String name2 = tests.randomCase("age");
            String name3 = tests.randomCase("course");
            query = "Select Students.%s, %s, students.%s from Students;";
            response = sendCommandToServer(String.format(query, name1, name2, name3));
            assertTrue(response.contains("[OK]"), String.format(failedMessage, query));
        }
        sendCommandToServer("DROP DATABASE school;");
    }

    @Test
    public void errorInsertTest() {
        String failedMessage = "This should be an exception since the wrong number of value is given";
        String response = sendCommandToServer("insert into students values ('ab1234');");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("insert into students values ('ab1234', 'NULL', 'NULL', 'NULL', 'NULL');");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        sendCommandToServer("DROP DATABASE school;");
    }

    @Test
    public void primaryKeyTest() {
        String failedMessage = "students should have a row with id = %d, but doesn't";
        String query = "INsert into students values ('ab123456', 'Micheal Wang', 24, 'Computer Science [conversion]');";
        for (int i=0; i < 100; i++) {
            sendCommandToServer(query);
        }
        for (int i=0; i < 100; i++) {
            query = "select id from Students where id == %d;";
            String response = sendCommandToServer(String.format(query, i+1));
            assertTrue(response.contains("[OK]"), "Select should succeed, but failed");
            assertTrue(response.contains(String.valueOf(i+1)), String.format(failedMessage, i+1));
        }
        sendCommandToServer("DROP DATABASE school;");
    }

    @Test
    public void primaryKeyReuseTest() {
        String failedMessage1 = "Table students should have a row with id = %d, but doesn't";
        String failedMessage2 = "Table students should not have a row with id = %d";
        String query = "INsert into students values ('ab123456', 'Micheal Wang', 24, 'Computer Science [conversion]');";
        sendCommandToServer(query);
        sendCommandToServer(query);
        sendCommandToServer(query);
        sendCommandToServer("DELETE FROM students WHERE id==2;");
        sendCommandToServer(query);
        String response = sendCommandToServer("SELECT id FROM students;");
        assertTrue(response.contains("[OK]"), String.format("[%s] should be a valid query", query));
        assertTrue(response.contains("4"), String.format(failedMessage1, 4));
        assertFalse(response.contains("2"), String.format(failedMessage2, 2));
        sendCommandToServer(query);
        response = sendCommandToServer("SELECT id FROM students;");
        assertTrue(response.contains("5"), String.format(failedMessage1, 5));
        sendCommandToServer("DROP DATABASE school;");
    }

    @Test
    public void insertEmptyTableTest() {
        sendCommandToServer("CREATE TABLE units;");
        String response = sendCommandToServer("INSERT INTO units values ('Computer Science (Conversion)');");
        assertTrue(response.contains("[ERROR]"), "Number of given value doesn't match number of columns");
        sendCommandToServer("DROP DATABASE school;");
    }

    @Test
    public void insertEmptyStringTest() {
        String failedMessage = "[%s] should be a valid query";
        String query = "INSERT INTO students VALUES ('ab1234', '', 13, '');";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format(failedMessage, query));
        response = sendCommandToServer("select id from students;");
        assertTrue(response.contains("1"), "[id] 1 should be in the result");
        sendCommandToServer("DROP DATABASE school;");
    }
}
