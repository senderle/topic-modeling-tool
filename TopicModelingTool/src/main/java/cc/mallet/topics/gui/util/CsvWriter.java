package cc.mallet.topics.gui.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.MalformedInputException;

import java.io.IOException;
import java.io.File;
import java.io.BufferedWriter;
import java.io.Closeable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;

import java.lang.Iterable;

// Like the CsvReader class, this handles exceptions incorrectly. When
// I have the time, I'll figure out The Right Way To Do It; in the
// meanwhile I'm leaving well enough alone.

public class CsvWriter implements Closeable {
    private Path inputPath = null;
    private String delim = ",";
    private String quote = "\"";
    private String eol = "\n";
    private BufferedWriter out = null;

    public CsvWriter(Path path, String delim, String quote, String eol) {
        inputPath = path;
        this.delim = delim;
        this.quote = quote;
        this.eol = eol;

        try {
            out = Files.newBufferedWriter(inputPath);
        } catch (IOException exc) {
            System.out.println(inputPath.toString() + ": Error opening file");
            throw new RuntimeException(exc);                                
        }
    }

    public CsvWriter(Path path, String delim, String quote) {
        this(path, delim, quote, "\n");
    }
    public CsvWriter(Path path, String delim) {
        this(path, delim, "\"", "\n");
    }
    public CsvWriter(Path path) {
        this(path, ",", "\"", "\n");
    }

    public CsvWriter(String path, String delim, String quote, String eol) {
        this(Paths.get(path), delim, quote, eol);
    }
    public CsvWriter(String path, String delim, String quote) {
        this(Paths.get(path), delim, quote);
    }
    public CsvWriter(String path, String delim) {
        this(Paths.get(path), delim);
    }
    public CsvWriter(String path) {
        this(Paths.get(path));
    }

    private String rowToString(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (String cell : row) {
            cell = cell.replaceAll(quote, quote + quote);
            if (cell.contains(delim) || 
                    cell.contains(eol) || 
                    cell.contains(quote)) {
                sb.append(quote);
                sb.append(cell);
                sb.append(quote);
            } else {
                sb.append(cell);
            }
            sb.append(delim);
        }
      
        // Remove trailing comma if present
        sb.setLength(sb.length() == 0 ? 0 : sb.length() - 1);

        return sb.toString();
    }

    private String cellsToString(Collection<String> cells) {
        return rowToString(cells.toArray(new String[cells.size()]));
    }

    public void writeRow(String[] row) {
        try {
            out.write(rowToString(row));
            out.newLine();
        } catch (IOException exc) {
            System.out.println(inputPath.toString() + ": Error writing file");
            throw new RuntimeException(exc);                                
        }
    }

    public void writeCellRow(Collection<String> cells) {
        try {
            out.write(cellsToString(cells));
            out.newLine();
        } catch (IOException exc) {
            System.out.println(inputPath.toString() + ": Error writing file");
            throw new RuntimeException(exc);                                
        }
    }

    public void writeRows(Collection<String[]> rows) {
        for (String[] row : rows) {
            writeRow(row);
        }
    }

    public void writeCellRows(Collection<Collection<String>> cellRows) {
        for (Collection<String> cellRow : cellRows) {
            writeCellRow(cellRow);
        }
    }

    public void close() {
        try {
            out.close();
        } catch (IOException exc) {
            System.out.println(inputPath.toString() + ": Error closing file");
            throw new RuntimeException(exc);                                
        }
    }

    public static void main (String[] args) throws IOException {
        String[] row1 = {"A", "B,C", "D"};
        String[] row2 = {"E", "F\nG", "H"};
        String[] row3 = {"I", "J\"K", "L"};
        String[] row4 = {"M", "NO", "P"};

        ArrayList<String[]> rows = new ArrayList<String[]>();
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);

        try (CsvWriter csv = new CsvWriter(args[0], ",")) {
            csv.writeRows(rows);
        }
    }
}
