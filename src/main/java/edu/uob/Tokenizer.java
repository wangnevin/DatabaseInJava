package edu.uob;

import edu.uob.command.KeyWords;

import java.util.ArrayList;
import java.util.Arrays;

public class Tokenizer {
    private String query;
    private final String[] specialCharacters = {"(",")",",",";","==",">=","<=","!=","<",">","="};
    private ArrayList<String> tokens = new ArrayList<String>();
    private int currentTokenIdx;

    public Tokenizer(String query) {
        this.query = query;
        this.currentTokenIdx = 0;
    }

    public ArrayList<String> generateTokens() {
        // Remove any whitespace at the beginning and end of the query
        this.query = this.query.trim();
        // Split the query on single quotes (to separate out query characters from string literals)
        String[] fragments = this.query.split("'");
        for (int i=0; i<fragments.length; i++) {
            // Every odd fragment is a string literal, so just append it without any alterations
            if (i%2 != 0) tokens.add("'" + fragments[i] + "'");
                // If it's not a string literal, it must be query characters (which need further processing)
            else {
                // Tokenise the fragments into an array of strings
                String[] nextBatchOfTokens = tokenise(fragments[i]);
                // Then add these to the "result" array list (needs a bit of conversion)
                tokens.addAll(Arrays.asList(nextBatchOfTokens));
            }
        }
        for (int i=0; i < this.tokens.size(); i++) {
            if (KeyWords.isKeyWord(this.tokens.get(i))) {
                this.tokens.set(i, this.tokens.get(i).toUpperCase());
            }
        }
        return this.tokens;
    }

    private String[] tokenise(String input) {
        // Add in some extra padding spaces around the "special characters"
        // so we can be sure that they are separated by AT LEAST one space (possibly more)
        for(int i=0; i < specialCharacters.length; i++) {
            input = input.replace(specialCharacters[i], " " + specialCharacters[i] + " ");
        }
        // Remove all double spaces (the previous replacements may have added some)
        // This is "blind" replacement - replacing if they exist, doing nothing if they don't
        while (input.contains("  ")) input = input.replaceAll("  ", " ");
        // Again, remove any whitespace from the beginning and end that might have been introduced
        input = input.trim();
        // Finally split on the space char (since there will now ALWAYS be a space between tokens)
        input = input.replace("< =", "<=");
        input = input.replace("> =", ">=");
        input = input.replace("= =", "==");
        input = input.replace("! =", "!=");
        return input.split(" ");
    }

    public String nextToken() {
        this.currentTokenIdx++;
        if (this.currentTokenIdx >= this.tokens.size()) { return ""; }
        return this.tokens.get(this.currentTokenIdx);
    }

    public String currentToken() {
        return this.tokens.get(currentTokenIdx);
    }

    public String getToken(int idx) { return tokens.get(idx); }
    public int getCurrentTokenIdx() { return this.currentTokenIdx; }

    public int getTokensSize() { return this.tokens.size(); }

    public void incrementTokenIdx() { this.currentTokenIdx++; }
    public void decrementTokenIdx() { this.currentTokenIdx--; }

    public boolean checkNextToken(String target) {
        if (this.currentTokenIdx + 1 >= this.tokens.size()) { return false; }
        return this.tokens.get(this.currentTokenIdx + 1).equals(target);
    }

    public String peekNextToken() {
        if (this.currentTokenIdx + 1 >= this.tokens.size()) { return ""; }
        return tokens.get(this.currentTokenIdx + 1);
    }
}
