package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateDropTests {
    TestGenerator tests;
    DBServer server;
    @BeforeEach
    public void Setup() {
        this.tests = new TestGenerator();
        this.server = new DBServer();
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> server.handleCommand(command),
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void parseRandomSpaceUseTest() {
        String failedMessage = "[%s] should be a valid query, but the parser judges it to be invalid";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        String query = "USE School;";
        for (int i=0; i < 100; i++) {
            String randQuery = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(randQuery);
            assertTrue(response.contains("[OK]"), String.format(failedMessage, randQuery));
        }
    }

    @Test
    public void parseRandomSpaceCreateDatabaseTest() {
        String failedMessage = "[%s] should be a valid query, but the parser judges it to be invalid";
        sendCommandToServer("DROP DATABASE School;");
        String query = "CREATE DATABASE School;";
        for (int i=0; i < 100; i++) {
            String randQuery = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(randQuery);
            assertTrue(response.contains("[OK]"), String.format(failedMessage, randQuery));
            sendCommandToServer("DROP DATABASE School;");
        }
    }

    @Test
    public void parseRandomSpaceCreateTableTest() {
        String failedMessage = "[%s] should be a valid query, but the parser judges it to be invalid";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        sendCommandToServer("USE School;");
        sendCommandToServer("DROP TABLE students;");
        String query = "CREATE TABLE students;";
        for (int i=0; i < 100; i++) {
            String randQuery = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(randQuery);
            assertTrue(response.contains("[OK]"), String.format(failedMessage, randQuery));
            sendCommandToServer("DROP TABLE students;");
        }
        sendCommandToServer("DROP TABLE students;");
        query = "CREATE TABLE students (name, age, unit, class);";
        for (int i=0; i < 100; i++) {
            String randQuery = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(randQuery);
            assertTrue(response.contains("[OK]"), String.format(failedMessage, randQuery));
            sendCommandToServer("DROP TABLE students;");
        }
        sendCommandToServer("DROP TABLE students;");
        query = "CREATE TABLE students (students.name, age, students.unit, class);";
        for (int i=0; i < 100; i++) {
            String randQuery = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(randQuery);
            assertTrue(response.contains("[OK]"), String.format(failedMessage, randQuery));
            sendCommandToServer("DROP TABLE students;");
        }
    }

    @Test
    public void parseRandomSpaceDropDatabaseTest() {
        String failedMessage = "[%s] should be a valid query, but the parser judges it to be invalid";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        String query = "DROP DATABASE School;";
        for (int i=0; i < 100; i++) {
            String randQuery = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(randQuery);
            assertTrue(response.contains("[OK]"), String.format(failedMessage, randQuery));
            sendCommandToServer("CREATE DATABASE School;");
        }
    }

    @Test
    public void parseRandomSpaceDropTableTest() {
        String failedMessage = "[%s] should be a valid query, but the parser judges it to be invalid";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        sendCommandToServer("USE School;");
        sendCommandToServer("CREATE TABLE students;");
        String query = "DROP TABLE students;";
        for (int i=0; i < 100; i++) {
            String randQuery = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(randQuery);
            assertTrue(response.contains("[OK]"), String.format(failedMessage, randQuery));
            sendCommandToServer("CREATE TABLE students;");
        }
    }

    @Test
    public void queryRandomSpaceCreateDatabaseTest() {
        String parseFailedMessage = "[%s] should be a valid query, but the parser judges it to be invalid";
        String queryFailedMessage = "[%s] should have been created, but USE [%s] failed.";
        sendCommandToServer("DROP DATABASE School;");
        String query = "CREATE DATABASE School;";
        for (int i=0; i < 100; i++) {
            String randQuery = tests.randomSpaceQuery(query);
            String response = sendCommandToServer(randQuery);
            assertTrue(response.contains("[OK]"), String.format(parseFailedMessage, randQuery));
            response = sendCommandToServer("USE School;");
            assertTrue(response.contains("[OK]"), String.format(queryFailedMessage, "School", "School"));
            sendCommandToServer("DROP DATABASE School;");
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void queryCaseInsensitiveDatabaseTest() {
        String failedMessage = "#%d/%d [%s] should exist, but not";
        String query = "USE %s;";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        for (int i=0; i < 100; i++) {
            String name = tests.randomCase("School");
            String response = sendCommandToServer(String.format(query, name));
            assertTrue(response.contains("[OK]"), String.format(failedMessage, i+1, 100, name));
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void queryCaseInsensitiveTableTest() {
        String failedMessage = "#%d/%d [%s] should exist, but not";
        String query = "SELECT * FROM %s;";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        sendCommandToServer("USE School;");
        sendCommandToServer("CREATE TABLE Students;");
        for (int i=0; i < 100; i++) {
            String name = tests.randomCase("Students");
            String response = sendCommandToServer(String.format(query, name));
            assertTrue(response.contains("[OK]"), String.format(failedMessage, i+1, 100, name));
        }
        sendCommandToServer("DROP DATABASE School;");
    }
}
