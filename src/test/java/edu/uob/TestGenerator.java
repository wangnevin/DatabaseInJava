package edu.uob;

import edu.uob.command.KeyWords;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class TestGenerator {
    private final String plainTextCh = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";;
    private final String symbolCh = "!#$%&*+-./:?@[\\]^_`{}~";
    private final String[] specialCharacters = {"(",")",",",";","==",">=","<=","!="};

    public String validDatabaseName(int len) {
        String result = validPlainText(len);
        if (KeyWords.isKeyWord(result)) { return "validName"; }
        return result;
    }

    public String invalidDatabaseName(int len) {
        return invalidPlainText(len);
    }

    public String validTableName(int len) {
        String result = validPlainText(len);
        if (KeyWords.isKeyWord(result)) { return "validName"; }
        return result;
    }

    public String invalidTableName(int len) {
        return invalidPlainText(len);
    }

    public String validAttributeName(int len, boolean dot) {
        String result = validPlainText(len);
        if (KeyWords.isKeyWord(result)) { result = "validName"; }
        if (dot) {
            return validTableName(len) + "." + result;
        }
        return result;
    }

    public String invalidAttributeName(int len, boolean dot) {
        if (dot) {
            return validTableName(len) + "." + invalidPlainText(len);
        }
        return invalidPlainText(len);
    }

    public String randomKeyWord() {
        Random random = new Random();
        int idx = random.nextInt(KeyWords.getKeyWords().length);
        String keyWord = KeyWords.getKeyWord(idx);
        StringBuilder result = new StringBuilder();
        for (int i=0; i < keyWord.length(); i++) {
            if (random.nextInt(10) >= 5) {
                result.append(Character.toLowerCase(keyWord.charAt(i)));
            } else {
                result.append(keyWord.charAt(i));
            }
        }
        return result.toString();
    }

    public String randomSpaceQuery(String input) {
        for (String specialCharacter : specialCharacters) {
            String LSpace = randomLengthSpace();
            String RSpace = randomLengthSpace();
            input = input.replace(specialCharacter, LSpace + specialCharacter + RSpace);
        }
        for (String keyWord: KeyWords.getKeyWords()) {
            String LSpace = randomLengthSpace();
            String RSpace = randomLengthSpace();
            input = input.replace(keyWord, LSpace + keyWord + RSpace);
        }
        return input;
    }

    public String randomCase(String input) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i=0; i < input.length(); i++) {
            if (random.nextInt(10) >= 5) {
                builder.append(Character.toLowerCase(input.charAt(i)));
            } else {
                builder.append(Character.toUpperCase(input.charAt(i)));
            }
        }
        return builder.toString();
    }

    public String randomStrLiteral(int len) {
        Random random = new Random();
        StringBuilder result = new StringBuilder("'");
        for (int i=0; i < len; i++) {
            int r = random.nextInt(12);
            if (r < 3) {
                result.append(" ");
            } else if (r < 6) {

            } else if (r < 9) {

            } else {

            }
        }
        return result.toString();
    }

    private String randomLengthSpace() {
        Random random = new Random();
        StringBuilder space = new StringBuilder();
        int spaceNum = random.nextInt(4);
        space.append(" ".repeat(spaceNum));
        return space.toString();
    }

    public String validPlainText(int len) {
        Random random = new Random();
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < len; i++) {
            stringBuffer.append(this.plainTextCh.charAt(random.nextInt(this.plainTextCh.length())));
        }
        return stringBuffer.toString();
    }

    private String invalidPlainText(int len) {
        Random random = new Random();
        StringBuilder strBuilder= new StringBuilder();
        strBuilder.append(validPlainText(len));
        int invalidNum = random.nextInt(1, len);
        for (int i=0; i < invalidNum; i++) {
            strBuilder.setCharAt(random.nextInt(len), this.symbolCh.charAt(random.nextInt(this.symbolCh.length())));
        }
        return strBuilder.toString();
    }
}
