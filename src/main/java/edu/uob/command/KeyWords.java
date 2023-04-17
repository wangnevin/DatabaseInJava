package edu.uob.command;

public class KeyWords {
    protected static final String[] keyWords = {
            "USE","CREATE","DROP","ALTER","INSERT","SELECT","UPDATE","DELETE",
            "FROM","WHERE","TABLE","DATABASE","INTO","SET","JOIN","ON","AND",
            "OR","LIKE","TRUE","FALSE","ADD", "VALUES"
    };
    public static String[] getKeyWords() { return keyWords; }

    public static String getKeyWord(int idx) { return keyWords[idx]; }

    public static boolean isKeyWord(String target) {
        for (String keyWord: keyWords) {
            if (target.toUpperCase().equals(keyWord)) {
                return true;
            }
        }
        return false;
    }
}
