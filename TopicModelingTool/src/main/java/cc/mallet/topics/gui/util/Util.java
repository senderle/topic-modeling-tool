package cc.mallet.topics.gui.util;

public class Util {
    public static String join(String delim, String... cells) {
        StringBuilder row = new StringBuilder();

        for (int i = 0; i < cells.length - 1; i += 1) {
            row.append(cells[i]);
            row.append(delim);
        }

        if (cells.length > 0) {
            row.append(cells[cells.length - 1]);
        }
        return row.toString();
    }

    public static int count(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count += 1;
            }
        }
        return count;
    }

}


