package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

public class SelectTests {
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
    public void basicSelectTest() {
        String failedMessage = "Result should contain %d, but not found";
        String query = "select age from students where age > 25;";
        String response = sendCommandToServer(query);
        for (int i=26; i <= 37; i++) {
            assertTrue(response.contains(String.valueOf(i)), String.format(failedMessage, i));
        }
        failedMessage = "Result should not contain %d, but found";
        for (int i=18; i<26; i++) {
            assertFalse(response.contains(String.valueOf(i)), String.format(failedMessage, i));
        }
        for (int i=38; i<50; i++) {
            assertFalse(response.contains(String.valueOf(i)), String.format(failedMessage, i));
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void selectAttributeTest() {
        String failedMessage = "Attributes in wrong order";
        String query = "select age, name, unit from students;";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("Age\tName\tUnit"), failedMessage);
        assertFalse(response.contains("Name\tAge\tUnit"), failedMessage);
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void selectConditionTest() {
        String query = "select * from students where %s;";
        for (int i=1; i <= 20;i++) {
            String response = sendCommandToServer(String.format(query, "id == " + i));
            assertTrue(response.contains(this.Code.get(i-1)));
            assertTrue(response.contains(this.Name.get(i-1)));
            if (i != 20) {
                assertFalse(response.contains(this.Name.get(i)));
                assertFalse(response.contains(this.Code.get(i)));
            }
        }
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void selectAndOrTest() {
        String failedMessage = "result should contain Name %s, but not found";
        String query = "select * from students where age > 25 and (unit == 8 or unit == 10 or unit == 15);";
        String response = sendCommandToServer(query);
        assertTrue(response.contains(this.Name.get(9)), String.format(failedMessage, this.Name.get(9)));
        assertTrue(response.contains(this.Name.get(14)), String.format(failedMessage, this.Name.get(14)));
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void basicPrioritySelectTest() {
        String query = "SELECT age, unit FROM students where (age < 25 or age > 32) and (unit >=2 and unit <= 16);";
        String failedMessage = "Row with Age = %d and Unit = %d is expected";

        String response = sendCommandToServer(query);
        assertTrue(response.contains("19") && response.contains("2"), String.format(failedMessage, 19, 2));
        assertTrue(response.contains("20") && response.contains("3"), String.format(failedMessage, 20, 3));
        assertTrue(response.contains("21") && response.contains("4"), String.format(failedMessage, 21, 4));
        assertTrue(response.contains("22") && response.contains("5"), String.format(failedMessage, 22, 5));
        assertTrue(response.contains("23") && response.contains("6"), String.format(failedMessage, 23, 6));
        assertTrue(response.contains("24") && response.contains("7"), String.format(failedMessage, 24, 7));
        assertTrue(response.contains("33") && response.contains("16"), String.format(failedMessage, 33, 16));
        failedMessage = "Row with Age = %d and Unit = %d is not expected";
        assertFalse(response.contains("18"), String.format(failedMessage, 18, 1));
        assertFalse(response.contains("25"), String.format(failedMessage, 25, 8));
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void prioritySelectTest() {
        String failedMessage = "[%s] should be a valid query";
        String query = "SELECT id, age, unit FROM students WHERE ((age < 23 or age > 25) and (unit >=2 or unit <= 16)) and " +
                "(id>=3 and id < 10);";
        String response = sendCommandToServer(query);
        assertTrue(response.contains("[OK]"), String.format(failedMessage, query));
        assertTrue(response.contains("20") && response.contains("21") && response.contains("22") && response.contains("26"),
                "Age column in result should contain 20, 21, 22, 26");
        assertFalse(response.contains("19") || response.contains("23") || response.contains("24") || response.contains("25"),
                "Age column in result should not contain 19, 23, 24, 25");
        sendCommandToServer("DROP DATABASE School;");
    }

    @Test
    public void invalidCompareTest() {
        String failedMessage = "[%s] is invalid compare";
        String query = "select age from students where age like 2;";
        String response = sendCommandToServer(query);
        assertFalse(response.contains("2"), String.format(failedMessage, "age like 2"));
        query = "select age from students where age like '2';";
        response = sendCommandToServer(query);
        assertFalse(response.contains("2"), String.format(failedMessage, "age like '2'"));
        sendCommandToServer("DROP DATABASE School;");
    }
}
