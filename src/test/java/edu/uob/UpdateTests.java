package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateTests {
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
    public void basicUpdateTest() {
        String failedMessage = "The %s should be %d";
        String query = "UPDATE students SET age=40, unit=20 where id > 2 and id <=10;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format("[%s] should be a valid query", query));
        for (int i=3; i <= 10; i++) {
            response = sendCommandToServer(String.format("SELECT age, unit FROM students WHERE id == %d;", i));
            assertTrue(response.contains("40"), String.format(failedMessage, "Age", 40));
            assertTrue(response.contains("20"), String.format(failedMessage, "Unit", 20));
        }
        failedMessage = "The %s should not be %d";
        for (int i=1; i < 3; i++) {
            response = sendCommandToServer(String.format("SELECT age, unit FROM students WHERE id == %d;", i));
            assertFalse(response.contains("40"), String.format(failedMessage, "Age", 40));
            assertFalse(response.contains("20"), String.format(failedMessage, "Unit", 20));
        }
        for (int i=11; i < 20; i++) {
            response = sendCommandToServer(String.format("SELECT age, unit FROM students WHERE id == %d;", i));
            assertFalse(response.contains("40"), String.format(failedMessage, "Age", 40));
            assertFalse(response.contains("20"), String.format(failedMessage, "Unit", 20));
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void attributeNameUpdateTest() {
        String failedMessage = "The %s should be %d";
        String query = "UPDATE students SET students.age=40, students.unit=20 where id > 2 and id <=10;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format("[%s] should be a valid query", query));
        for (int i=3; i <= 10; i++) {
            response = sendCommandToServer(String.format("SELECT age, unit FROM students WHERE id == %d;", i));
            assertTrue(response.contains("40"), String.format(failedMessage, "Age", 40));
            assertTrue(response.contains("20"), String.format(failedMessage, "Unit", 20));
        }
        failedMessage = "The %s should not be %d";
        for (int i=1; i < 3; i++) {
            response = sendCommandToServer(String.format("SELECT age, unit FROM students WHERE id == %d;", i));
            assertFalse(response.contains("40"), String.format(failedMessage, "Age", 40));
            assertFalse(response.contains("20"), String.format(failedMessage, "Unit", 20));
        }
        for (int i=11; i < 20; i++) {
            response = sendCommandToServer(String.format("SELECT age, unit FROM students WHERE id == %d;", i));
            assertFalse(response.contains("40"), String.format(failedMessage, "Age", 40));
            assertFalse(response.contains("20"), String.format(failedMessage, "Unit", 20));
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void invalidUpdateTest() {
        String failedMessage = "[%s] should be invalid query";
        String query = "UPDATE students SET %s = -1 WHERE id == 1;";
        for (int i=0; i < 100; i++) {
            String attributeName = tests.validAttributeName(10, false);
            String response = sendCommandToServer(String.format(query, attributeName));
            assertTrue(response.contains("[ERROR]"), failedMessage);
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void updateIdTest() {
        String failedMessage = "[id] column should not be allowed to be changed";
        String response = sendCommandToServer("UPDATE students SET id = 50 WHERE id == 10;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("UPDATE students SET Id = 50 WHERE id == 10;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("UPDATE students SET iD = 50 WHERE id == 10;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        response = sendCommandToServer("UPDATE students SET ID = 50 WHERE id == 10;");
        assertTrue(response.contains("[ERROR]"), failedMessage);
        sendCommandToServer("DROP DATABASE School;");
    }
}
