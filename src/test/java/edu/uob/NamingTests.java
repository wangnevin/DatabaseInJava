package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NamingTests {
    TestGenerator tests;
    DBServer server;
    @BeforeEach
    public void Setup() {
        this.tests = new TestGenerator();
        this.server = new DBServer();
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void parseDatabaseNameTest() {
        String failedMessage = "[%s] should be a valid database name, but the parser judges it to be invalid";
        String query = "CREATE DATABASE %s;";
        for (int i=0; i < 100; i++) {
            String validName = tests.validDatabaseName(10);
            String response = sendCommandToServer(String.format(query, validName));
            assertTrue(response.contains("[OK]"), String.format(failedMessage, validName));
            sendCommandToServer(String.format("DROP DATABASE %s;", validName));
        }
        failedMessage = "[%s] should be an invalid database name, but the parser judges it to be valid";
        for (int i=0; i < 100; i++) {
            String invalidName = tests.invalidDatabaseName(10);
            String response = sendCommandToServer(String.format(query, invalidName));
            assertTrue(response.contains("[ERROR]"), String.format(failedMessage, invalidName));
            sendCommandToServer(String.format("DROP DATABASE %s;", invalidName));
        }
    }

    @Test
    public void parseTableNameTest() {
        String failedMessage = "[%s] should be a valid table name, but the parser judges it to be invalid";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        sendCommandToServer("USE School;");
        String query = "CREATE TABLE %s;";
        for (int i=0; i < 100; i++) {
            String validName = tests.validTableName(10);
            String response = sendCommandToServer(String.format(query, validName));
            assertTrue(response.contains("[OK]"), String.format(failedMessage, validName));
            sendCommandToServer(String.format("DROP TABLE %s;", validName));
        }
        failedMessage = "[%s] should be an invalid table name, but the parser judges it to be valid";
        for (int i=0; i < 100; i++) {
            String invalidName = tests.invalidTableName(10);
            String response = sendCommandToServer(String.format(query, invalidName));
            assertTrue(response.contains("[ERROR]"), String.format(failedMessage, invalidName));
            sendCommandToServer(String.format("DROP TABLE %s;", invalidName));
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void parseAttributeNameWithoutDot() {
        String failedMessage = "[%s | %s | %s] should be valid attribute names, but the parser judges them to be invalid";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        sendCommandToServer("USE School;");
        String query = "CREATE TABLE students (%s, %s, %s);";
        for (int i=0; i < 100; i++) {
            String validName1 = tests.validAttributeName(10, false);
            String validName2 = tests.validAttributeName(10, false);
            String validName3 = tests.validAttributeName(10, false);
            String response = sendCommandToServer(String.format(query, validName1, validName2, validName3));
            assertTrue(response.contains("[OK]"), String.format(failedMessage, validName1, validName2, validName3));
            sendCommandToServer("DROP TABLE students;");
        }
        failedMessage = "[%s | %s | %s] should be invalid attribute names, but the parser judges them to be valid";
        for (int i=0; i < 100; i++) {
            String invalidName1 = tests.invalidAttributeName(10, false);
            String invalidName2 = tests.invalidAttributeName(10, false);
            String invalidName3 = tests.invalidAttributeName(10, false);
            String response = sendCommandToServer(String.format(query, invalidName1, invalidName2, invalidName3));
            assertTrue(response.contains("[ERROR]"), String.format(failedMessage, invalidName1, invalidName2, invalidName3));
            sendCommandToServer("DROP TABLE students;");
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void parseAttributeNameWithDot() {
        String failedMessage = "[%s | %s | %s] should be valid attribute names, but the parser judges them to be invalid";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        sendCommandToServer("USE School;");
        String query = "CREATE TABLE students (%s, %s, %s);";
        for (int i=0; i < 100; i++) {
            String validName1 = tests.validAttributeName(10, true);
            String validName2 = tests.validAttributeName(10, true);
            String validName3 = tests.validAttributeName(10, true);
            String response = sendCommandToServer(String.format(query, validName1, validName2, validName3));
            assertTrue(response.contains("[OK]"), String.format(failedMessage, validName1, validName2, validName3));
            sendCommandToServer("DROP TABLE students;");
        }
        failedMessage = "[%s | %s | %s] should be invalid attribute names, but the parser judges them to be valid";
        for (int i=0; i < 100; i++) {
            String invalidName1 = tests.invalidAttributeName(10, true);
            String invalidName2 = tests.invalidAttributeName(10, true);
            String invalidName3 = tests.invalidAttributeName(10, true);
            String response = sendCommandToServer(String.format(query, invalidName1, invalidName2, invalidName3));
            assertTrue(response.contains("[ERROR]"), String.format(failedMessage, invalidName1, invalidName2, invalidName3));
            sendCommandToServer("DROP TABLE students;");
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void parseNameUsingKeyWord() {
        String failedMessage = "#%d/%d [%s] should be invalid database names, but the parser judges them to be valid";
        String query = "DROP DATABASE %s;";
        for (int i=0; i < 100; i++) {
            String name = tests.randomKeyWord();
            String response = sendCommandToServer(String.format(query, name));
            assertTrue(response.contains("[ERROR]"), String.format(failedMessage, i+1, 100, name));
        }

        failedMessage = "#%d/%d [%s] should be invalid table names, but the parser judges them to be valid";
        query = "DROP TABLE %s";
        for (int i=0; i < 100; i++) {
            String name = tests.randomKeyWord();
            String response = sendCommandToServer(String.format(query, name));
            assertTrue(response.contains("[ERROR]"), String.format(failedMessage, i+1, 100, name));
        }

        failedMessage = "#%d/%d [%s] should be invalid attribute names, but the parser judges them to be valid";
        sendCommandToServer("DROP DATABASE School;");
        sendCommandToServer("CREATE DATABASE School;");
        query = "CREATE TABLE students (%s);";
        for (int i=0; i < 100; i++) {
            String name = tests.randomKeyWord();
            String response = sendCommandToServer(String.format(query, name));
            assertTrue(response.contains("[ERROR]"), String.format(failedMessage, i+1, 100, name));
        }
        sendCommandToServer("DROP DATABASE School;");
    }
}
