package cc.mallet.topics.gui.util;

import java.util.List;

public class Util {
    private static String eol = "\n";

    public static String joinAll(String delim, String[] cells) {
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

    public static String joinAll(String delim, List<String> cells) {
        return joinAll(delim, cells.toArray(new String[cells.size()]));
    }

    public static String join(String delim, String... cells) {
        return joinAll(delim, cells);
    }

    public static String joinQuoted(
            String delim,
            String quote,
            String[] cells
    ) {
        StringBuilder row = new StringBuilder();
        for (String cell : cells) {
            cell = cell.replaceAll(quote, quote + quote);
            if (cell.contains(delim) ||
                    cell.contains(eol) ||
                    cell.contains(quote)) {
                row.append(quote);
                row.append(cell);
                row.append(quote);
            } else {
                row.append(cell);
            }
            row.append(delim);
        }

        // Remove trailing comma if present
        row.setLength(row.length() == 0 ? 0 : row.length() - 1);

        return row.toString();
    }

    public static String joinQuoted(
            String delim,
            String quote,
            List<String> cells
    ) {
        return joinQuoted(
                delim, quote, cells.toArray(new String[cells.size()])
        );
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

    public static int count(String s, String delim) {
        int count = 0;
        for (int i = 0; i < s.length() - delim.length() + 1; i++) {
            if (s.substring(i, i + delim.length()).equals(delim)) {
                count += 1;
            }
        }
        return count;
    }
}


