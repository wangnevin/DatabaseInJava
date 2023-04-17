package edu.uob;

import edu.uob.command.*;

public class Parser {
    private Tokenizer tokenizer;
    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public DBcmd parse() throws Exception {
        String currentToken = tokenizer.currentToken();
        switch (currentToken.toUpperCase()) {
            case "USE" -> { return new UseCMD(tokenizer); }
            case "SELECT" -> { return new SelectCMD(tokenizer); }
            case "CREATE" -> { return new CreateCMD(tokenizer); }
            case "DROP" -> { return new DropCMD(tokenizer); }
            case "INSERT" -> { return new InsertCMD(tokenizer); }
            case "JOIN" -> { return new JoinCMD(tokenizer); }
            case "ALTER" -> { return new AlterCMD(tokenizer); }
            case "UPDATE" -> { return new UpdateCMD(tokenizer); }
            case "DELETE" -> { return new DeleteCMD(tokenizer); }
            default -> throw new Exception("Invalid Command: " + tokenizer.nextToken());
        }
    }
}
