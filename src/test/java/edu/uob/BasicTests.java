package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class BasicTests {
    DBServer server;
    @BeforeEach
    public void setup() {
        this.server = new DBServer();
        sendCommandToServer("drop database Student;");
        sendCommandToServer("create database Student;");
        sendCommandToServer("USE Student;");

        sendCommandToServer("create table students (name, age, unit);");
        sendCommandToServer("insert into students values ('Jake', 24, 2);");
        sendCommandToServer("insert into students values ('Mike', 23, 1);");
        sendCommandToServer("insert into students values ('Nevin', 25, 3);");
        sendCommandToServer("insert into students values ('Micheal', 22, 3);");
        sendCommandToServer("insert into students values ('Chris', 26, 5);");
        sendCommandToServer("insert into students values ('Adam', 21, 4);");

        sendCommandToServer("CREATE TABLE units (name);");
        sendCommandToServer("INSERT INTO units VALUES ('Computer Science');");
        sendCommandToServer("INSERT INTO units VALUES ('Chemistry');");
        sendCommandToServer("INSERT INTO units VALUES ('Physics');");
        sendCommandToServer("INSERT INTO units VALUES ('Law');");
        sendCommandToServer("INSERT INTO units VALUES ('Medical');");

        sendCommandToServer("drop database people;");
        sendCommandToServer("create database people;");
        sendCommandToServer("use people;");

        sendCommandToServer("create table people (Name, Age, Email);");
        sendCommandToServer("insert into people values ('Bob', 21, 'bob@bob.net');");
        sendCommandToServer("insert into people values ('Harry', 32, 'harry@harry.com');");
        sendCommandToServer("insert into people values ('Chris', 42, 'chris@chris.ac.uk');");
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }
    @Test
    public void basicUseCMDTest() {
        sendCommandToServer("DROP DATABASE People;");
        sendCommandToServer("CREATE DATABASE People;");
        String response = sendCommandToServer("USE People;");
        assertTrue(response.contains("[OK]"), "Response should contain [OK], but not.");

        sendCommandToServer("DROP DATABASE student;");
        sendCommandToServer("DROP DATABASE People;");
    }

    @Test
    public void basicCreateCMDTest() {
        sendCommandToServer("DROP DATABASE Student;");
        String response = sendCommandToServer("CREATE DATABASE Student;");
        assertTrue(response.contains("[OK]"), "Response should contain [OK], but not.");

        sendCommandToServer("USE Student;");
        response = sendCommandToServer("CREATE TABLE students;");
        assertTrue(response.contains("[OK]"), "Response should contain [OK], but not.");

        response = sendCommandToServer("CREATE TABLE units (id, name, mark);");
        assertTrue(response.contains("[ERROR]"), "Response should contain [ERROR], but not.");

        sendCommandToServer("DROP DATABASE Student;");
        sendCommandToServer("DROP DATABASE People;");
    }

    @Test
    public void basicSelectCMDTest() {
        String failedMessage = "Response should contain [%s]";
        String failedMessage2 = "Response should not contain [%s]";
        sendCommandToServer("USE People;");

        String response = sendCommandToServer("select * from people where Name like 'Bob';");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertFalse(response.contains("Harry"), String.format(failedMessage2, "Harry"));
        assertFalse(response.contains("Chris"), String.format(failedMessage2, "Chris"));

        response = sendCommandToServer("select * from people where ((Age >=21) AND Age <50);");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where ((Age >=21) AND (Age <50));");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where (Age >=21 AND (Age <50));");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where (Age >=21 AND Age <50);");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where Age >=21 AND Age <50;");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where (Age >=21) AND (Age <50);");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where (Age >=21) AND Age <50;");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where Age >=21 AND (Age <50);");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where Email like 'bob';");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertFalse(response.contains("Harry"), String.format(failedMessage2, "Harry"));
        assertFalse(response.contains("Chris"), String.format(failedMessage2, "Chris"));

        response = sendCommandToServer("select * from people where Age <=21 or (Age >40);");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertFalse(response.contains("Harry"), String.format(failedMessage2, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where (Age <=45 and (Name=='Bob' or Name == 'Chris'));");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertFalse(response.contains("Harry"), String.format(failedMessage2, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people ;");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Bob"), String.format(failedMessage, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where Name like 'Bob' AND Age < 20 OR Age >30;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));
        assertTrue(response.contains("Harry"), String.format(failedMessage, "Harry"));
        assertTrue(response.contains("Chris"), String.format(failedMessage, "Chris"));

        response = sendCommandToServer("select * from people where Name like 'Bob' AND (Age < 20 OR Age >30);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));
        assertFalse(response.contains("Harry"), String.format(failedMessage2, "Harry"));
        assertFalse(response.contains("Chris"), String.format(failedMessage2, "Chris"));

        response = sendCommandToServer("SELECT name FROM people WHERE name == 'bob';");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertFalse(response.contains("Bob"), String.format(failedMessage2, "Bob"));

        sendCommandToServer("DROP DATABASE student;");
        sendCommandToServer("DROP DATABASE People;");
    }

    @Test
    public void selectAttributeTest() {
        String failedMessage = "Response should contain [%s]";
        String failedMessage2 = "Response should not contain [%s]";

        sendCommandToServer("USE People;");
        String response = sendCommandToServer("select Age, Email from people where (Age >=21) AND Age <50;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertTrue(response.contains("Age") && response.contains("Email"), String.format(failedMessage, "Age | Email"));
        assertFalse(response.contains("Name") || response.contains("id"), String.format(failedMessage2, "Name | id"));

        response = sendCommandToServer("select Email, people.Age from people where (Age >=21) AND Age <50;");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("Age") && response.contains("Email"), String.format(failedMessage, "Age | Email"));
        assertFalse(response.contains("Name") || response.contains("id"), String.format(failedMessage2, "Name | id"));

        response = sendCommandToServer("select Name, Email from people where none > 21;");
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, "ERROR"));

        sendCommandToServer("DROP DATABASE student;");
        sendCommandToServer("DROP DATABASE People;");
    }

    @Test
    public void basicJoinCMDTest() {
        String failedMessage = "Response should contain [%s]";
        String failedMessage2 = "Response should not contain [%s]";
        sendCommandToServer("USE student;");

        String response = sendCommandToServer("join students and units on students.unit and units.id;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertTrue(response.contains("students.name") && response.contains("students.age") && response.contains("units.name"),
                String.format(failedMessage, "students.name|students.age|units.name"));
        assertFalse(response.contains("students.unit") || response.contains("units.id"), String.format(failedMessage2,
                "students.unit|units.id"));

        response = sendCommandToServer("join students and units on unit and id;");
        assertTrue(response.contains("[OK]"));
        assertTrue(response.contains("students.name") && response.contains("students.age") && response.contains("units.name"),
                String.format(failedMessage, "students.name|students.age|units.name"));
        assertFalse(response.contains("students.unit") || response.contains("units.id"), String.format(failedMessage2,
                "students.unit|units.id"));

        sendCommandToServer("DROP DATABASE student;");
        sendCommandToServer("DROP DATABASE People;");
    }

    @Test
    public void basicAlterCMDTest() {
        sendCommandToServer("USE student;");
        String failedMessage = "Response should contain [%s]";

        String response = sendCommandToServer("alter table students add mark;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("select mark from students;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        assertTrue(response.contains("mark"), String.format(failedMessage, "mark"));

        sendCommandToServer("DROP DATABASE student;");
        sendCommandToServer("DROP DATABASE People;");
    }

    @Test
    public void basicUpdateTest() {
        sendCommandToServer("USE student;");
        String failedMessage = "Response should contain [%s]";

        String response = sendCommandToServer("UPDATE students SET age=25 where name=='Jake';");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("select age from students where name == 'Jake';");
        assertTrue(response.contains("age") && response.contains("25"), String.format(failedMessage, "age|25"));

        response = sendCommandToServer("UPDATE students SET students.age=26 where name=='Jake';");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("select age from students where name == 'Jake';");
        assertTrue(response.contains("age") && response.contains("26"), String.format(failedMessage, "age|26"));

        response = sendCommandToServer("UPDATE students SET age=27 where students.name=='Jake' and students.id==1;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("select age from students where name == 'Jake';");
        assertTrue(response.contains("age") && response.contains("27"), String.format(failedMessage, "age|27"));

        response = sendCommandToServer("UPDATE students SET students.age=28 where (students.name=='Jake' and students.id==1);");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("select age from students where name == 'Jake';");
        assertTrue(response.contains("age") && response.contains("28"), String.format(failedMessage, "age|28"));

        response = sendCommandToServer("UPDATE students SET age=25 where none=='Jake';");
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, "ERROR"));

        sendCommandToServer("DROP DATABASE student;");
        sendCommandToServer("DROP DATABASE People;");
    }

    @Test
    public void basicDeleteTest() {
        String failedMessage = "Response should contain [%s]";
        String failedMessage2 = "Response should not contain [%s]";
        sendCommandToServer("USE student;");

        String response = sendCommandToServer("DELETE FROM students WHERE unit == 3;");
        assertTrue(response.contains("[OK]"), String.format(failedMessage, "OK"));
        response = sendCommandToServer("SELECT name, unit FROM students;");
        assertTrue(response.contains("Jake") && response.contains("Mike") &&
                response.contains("Chris") && response.contains("Adam"), String.format(failedMessage, "Jake|Mike|Chris|Adam"));
        assertTrue(response.contains("1") && response.contains("2") && response.contains("4") &&
                response.contains("5"), String.format(failedMessage, "1|2|4|5"));
        assertFalse(response.contains("Nevin") || response.contains("Micheal"), String.format(failedMessage2, "Nevin|Micheal"));
        assertFalse(response.contains("3"), String.format(failedMessage2, "3"));

        response = sendCommandToServer("DELETE FROM students WHERE students.none == 3;");
        assertTrue(response.contains("[ERROR]"), String.format(failedMessage, "ERROR"));

        sendCommandToServer("DROP DATABASE student;");
        sendCommandToServer("DROP DATABASE People;");
    }
}
