package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class TokenizerTest {
    @BeforeEach
    public void setup() {

    }

    @Test
    public void testBasicQuery() {
        String query = "  INSERT  INTO  people   VALUES(  'Simon Lock'  ,35, 'simon@bristol.ac.uk' , 1.8  ) ; ";
        Tokenizer tokenizer = new Tokenizer(query);
        ArrayList<String> tokens = tokenizer.generateTokens();
        assertEquals("INSERT", tokenizer.currentToken());
        assertEquals("INTO", tokenizer.nextToken());
        assertEquals("people", tokenizer.nextToken());
        assertEquals("VALUES", tokenizer.nextToken());
        assertEquals("(", tokenizer.nextToken());
        assertEquals("'Simon Lock'", tokenizer.nextToken());
        assertEquals(",", tokenizer.nextToken());
        assertEquals("35", tokenizer.nextToken());
        assertEquals(",", tokenizer.nextToken());
        assertEquals("'simon@bristol.ac.uk'", tokenizer.nextToken());
        assertEquals(",", tokenizer.nextToken());
        assertEquals("1.8", tokenizer.nextToken());
        assertEquals(")", tokenizer.nextToken());
        assertEquals(";", tokenizer.nextToken());
        assertEquals(14, tokens.size());
    }

    @Test
    public void testKeyWordCase() {
        String query = "  Insert  into  people   VALUES(  'Simon Lock'  ,35, 'simon@bristol.ac.uk' , 1.8  ) ; ";
        Tokenizer tokenizer = new Tokenizer(query);
        ArrayList<String> tokens = tokenizer.generateTokens();
        assertEquals("INSERT", tokenizer.currentToken());
        assertEquals("INTO", tokenizer.nextToken());
        assertEquals("people", tokenizer.nextToken());
        assertEquals("VALUES", tokenizer.nextToken());
        assertEquals("(", tokenizer.nextToken());
        assertEquals("'Simon Lock'", tokenizer.nextToken());
        assertEquals(14, tokens.size());
    }

    @Test
    public void testCondition() {
        String query1 = " Select * from people where (age>=20 AND income < 4000 AND name like 'simon lock');";
        assertTestCondition(query1);
        String query2 = "Select * from people where (age >=20 AND income < 4000 AND name like 'simon lock');";
        assertTestCondition(query2);
        String query3 = "Select * from people where (age>= 20 AND income < 4000 AND name like 'simon lock');";
        assertTestCondition(query3);
        String query4 = "Select * from people where (age >= 20 AND income<4000 AND name like 'simon lock');";
        assertTestCondition(query4);
        String query5 = "Select * from people where (age >= 20 AND income <4000 AND name like 'simon lock');";
        assertTestCondition(query5);
        String query6 = "Select * from people where (age >= 20 AND income< 4000 AND name like 'simon lock');";
        assertTestCondition(query4);
    }

    private void assertTestCondition(String query) {
        Tokenizer tokenizer = new Tokenizer(query);
        tokenizer.generateTokens();
        assertEquals("SELECT", tokenizer.currentToken());
        assertEquals("*", tokenizer.nextToken());
        assertEquals("FROM", tokenizer.nextToken());
        assertEquals("people", tokenizer.nextToken());
        assertEquals("WHERE", tokenizer.nextToken());
        assertEquals("(", tokenizer.nextToken());
        assertEquals("age", tokenizer.nextToken());
        assertEquals(">=", tokenizer.nextToken());
        assertEquals("20", tokenizer.nextToken());
        assertEquals("AND", tokenizer.nextToken());
        assertEquals("income", tokenizer.nextToken());
        assertEquals("<", tokenizer.nextToken());
        assertEquals("4000", tokenizer.nextToken());
    }

    @Test
    public void testNameValuePair() {
        String query = "UPDATE students SET marks=78.8,name='Micheal' where id >= 1";
        Tokenizer tokenizer = new Tokenizer(query);
        List<String> tokens = tokenizer.generateTokens();
    }
}
