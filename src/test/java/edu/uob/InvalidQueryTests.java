package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class InvalidQueryTests {
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
    public void missingKeyWord() {
        String failedMessage = "[%s] is missing [%s]";
        String query = "CREATE units;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, query, "TABLE | DATABASE"));
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void missingSemiColon() {
        String failedMessage = "[%s] is missing [%s]";
        String query = "CREATE TABLE units";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, query, ";"));
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void extraToken() {
        String failedMessage = "[%s] has extra token after semicolon";
        String query = "CREATE TABLE units; SELECT * FROM units;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, query));
        sendCommandToServer("DROP DATABASE School;");
    }
}
