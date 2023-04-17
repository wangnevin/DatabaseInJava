package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleScriptTests {
    DBServer server;
    @BeforeEach
    public void Setup() {
        this.server = new DBServer();
        String failedMessage = "Response should contain [%s]";
        sendCommandToServer("DROP DATABASE markbook;");
        String response = sendCommandToServer("CREATE DATABASE markbook;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("USE markbook;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));

        response = sendCommandToServer("CREATE TABLE coursework (task, submission);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void markBookTest() {
        String failedMessage = "Response should contain [%s]";
        String failedMessage2 = "Response should not contain [%s]";

        String response = sendCommandToServer("SELECT * FROM marks WHERE name != 'Dave';");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertTrue(response.contains("Steve"), String.format(failedMessage, "Steve"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Clive"), String.format(failedMessage, "Clive"));
        assertFalse(response.contains("Dave"), String.format(failedMessage2, "Dave"));

        response = sendCommandToServer("SELECT * FROM marks WHERE pass == TRUE;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertTrue(response.contains("Steve"), String.format(failedMessage, "Steve"));
        assertTrue(response.contains("Dave"), String.format(failedMessage, "Dave"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));
        assertFalse(response.contains("Clive"), String.format(failedMessage2, "Clive"));
        sendCommandToServer("DROP DATABASE markbook;");
    }

    @Test
    public void courseWorkTest() {
        String failedMessage = "Response should contain [%s]";
        String failedMessage2 = "Response should not contain [%s]";

        String response = sendCommandToServer("SELECT * FROM coursework;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertTrue(response.contains("OXO"), String.format(failedMessage, "OXO"));
        assertTrue(response.contains("DB"), String.format(failedMessage, "DB"));
        assertTrue(response.contains("STAG"), String.format(failedMessage, "STAG"));

        response = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));

        response = sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));

        response = sendCommandToServer("SELECT * FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("Clive"), String.format(failedMessage, "Clive"));
        assertFalse(response.contains("Steve"), String.format(failedMessage2, "Steve"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));
        assertFalse(response.contains("Dave"), String.format(failedMessage2, "Dave"));

        response = sendCommandToServer("DELETE FROM marks WHERE name == 'Dave';");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(response.contains("Dave"), String.format(failedMessage2, "Dave"));
        assertTrue(response.contains("Steve"), String.format(failedMessage, "Steve"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Clive"), String.format(failedMessage, "Clive"));

        response = sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);");
        assertTrue(response.contains("Clive"), String.format(failedMessage, "Clive"));
        assertFalse(response.contains("Steve"), String.format(failedMessage2, "Steve"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));
        assertFalse(response.contains("Dave"), String.format(failedMessage2, "Dave"));

        response = sendCommandToServer("SELECT * FROM marks WHERE name LIKE 've';");
        assertTrue(response.contains("Clive"), String.format(failedMessage, "Clive"));
        assertTrue(response.contains("Steve"), String.format(failedMessage, "Steve"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));
        assertFalse(response.contains("Dave"), String.format(failedMessage2, "Dave"));

        response = sendCommandToServer("SELECT id FROM marks WHERE pass == FALSE;");
        assertTrue(response.contains("3"), String.format(failedMessage, "3"));
        assertTrue(response.contains("4"), String.format(failedMessage, "4"));
        assertFalse(response.contains("1"), String.format(failedMessage2, "1"));
        assertFalse(response.contains("2"), String.format(failedMessage2, "2"));

        response = sendCommandToServer("SELECT name FROM marks WHERE mark>60;");
        assertTrue(response.contains("Steve"), String.format(failedMessage, "Steve"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));
        assertFalse(response.contains("Dave"), String.format(failedMessage2, "Dave"));
        assertFalse(response.contains("Clive"), String.format(failedMessage2, "Clive"));

        response = sendCommandToServer("DELETE FROM marks WHERE mark<40;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), String.format(failedMessage, "Steve"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));
        assertFalse(response.contains("Dave"), String.format(failedMessage2, "Dave"));
        assertFalse(response.contains("Clive"), String.format(failedMessage2, "Clive"));

        response = sendCommandToServer("SELECT * FROM marks");
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, "ERROR"));
        response = sendCommandToServer("SELECT * FROM crew;");
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, "ERROR"));
        response = sendCommandToServer("SELECT * FROM marks pass == TRUE;");
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, "ERROR"));
        sendCommandToServer("DROP DATABASE markbook;");
    }
}
