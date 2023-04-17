package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteTests {
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
    public void basicDeleteTest() {
        String failedMessage = "[%s] should be a valid query";
        String query = "DELETE FROM students WHERE (age > 22 and age < 26) or (age > 28 and age < 33);";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format(failedMessage, query));
        response = sendCommandToServer("select age from students;");
        failedMessage = "Result should not contain age %d";
        for (int i=23; i < 26; i++) {
            assertFalse(response.contains(String.valueOf(i)), String.format(failedMessage, i));
        }
        for (int i=29; i < 33; i++) {
            assertFalse(response.contains(String.valueOf(i)), String.format(failedMessage, i));
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void deleteTest() {
        String failedMessage = "[%s] should be a valid query";
        String query = "DELETE FROM students WHERE ((age > 22 and age < 26) or (age > 28 and age < 33)) and (id == 7 or id == 8 or id==13 or id==14);";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format(failedMessage, query));
        response = sendCommandToServer("select age from students;");
        failedMessage = "Result should contain %d";
        assertTrue(response.contains("23"), String.format(failedMessage, 23));
        assertTrue(response.contains("29"), String.format(failedMessage, 29));
        assertTrue(response.contains("32"), String.format(failedMessage, 32));
        sendCommandToServer("DROP DATABASE School;");
    }
}
