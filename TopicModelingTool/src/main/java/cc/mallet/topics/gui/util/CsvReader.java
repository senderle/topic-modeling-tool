package cc.mallet.topics.gui.util;

import cc.mallet.topics.gui.util.Util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.MalformedInputException;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.Closeable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import java.lang.Iterable;

// The way this class handles exceptions is not right. But I don't yet know
// what the right way to handle them is. So I'm leaving well enough 
// alone for now. 

// TODO: Figure out how to correctly handle different character sets.

public class CsvReader implements Iterable<String[]> {
    private Path inputPath = null;
    private String delim = ",";
    private int headerLines = 0;
    private ArrayList<String[]> headers = new ArrayList<String[]>();

    public CsvReader(Path inputPath, String delim, int headerLines) {
        this.inputPath = inputPath;
        this.delim = delim;
        this.headerLines = headerLines;
        readHeaders();
    }

    public CsvReader(String inputPath, String delim, int headerLines) {
        this(Paths.get(inputPath), delim, headerLines);
    }

    public CsvReader(File inputPath, String delim, int headerLines) {
        this(inputPath.toPath(), delim, headerLines);
    }

    public Iterator<String[]> iterator() {
        return new CsvRowIterator();
    }

    // Read the header lines and store them; this means the headers are
    // available separately from the row iterator below. (Should this 
    // return an ArrayList<String> instead of modifying `headers` directly?)
    private void readHeaders() {
        String[] headerRow = null;
        Iterator <String[]> csvRows = new CsvRowIterator(false);
        for (int i = 0; i < headerLines; i++) {
            headerRow = csvRows.next();
            if (headerRow != null) {
                headers.add(headerRow);
            }
        }
    }

    private class CsvRowIterator implements Iterator<String[]> {
        private BufferedReader inputReader = null;
        private int linecount = 0;
        private int rowcount = 0;
        private boolean fileOpen = false;
        private String[] nextRow = null;
        public CsvRowIterator(boolean discardHeaders) {
            try {
                inputReader = Files.newBufferedReader(inputPath);
                fileOpen = true;
            } catch (IOException exc) {
                System.out.println(inputPath.toString() + ": Error opening file");
                throw new RuntimeException(exc);
            }

            if (discardHeaders) {
                String[] headerRow = null;
                for (int i = 0; i < headerLines; i++) {
                    headerRow = readRow();
                }
                nextRow = readRow();
            }
        }

        public CsvRowIterator() {
            // Discard headers by default; it will be stored when the
            // CsvReader constructor is first called.
            this(true);
        }

        public boolean hasNext() {
            return (nextRow != null);
        }

        public String[] next() {
            String[] result = nextRow;
            nextRow = readRow();
            return result;
        }

        private void close() {
            fileOpen = false;
            try {
                inputReader.close();
            } catch (IOException exc) {
                System.out.println(
                        inputPath.toString() + ": Error closing file."
                );
                throw new RuntimeException(exc);
            }
        }

        private String[] readRow() {
            String line;
            try {
                line = readLogicalLine();
            } catch (IOException exc) {
                System.out.println(
                        "Error on line " + linecount + 
                        " of " + inputPath.toString()
                );
                throw new RuntimeException(exc);
            }

            if (line == null) {
                if (fileOpen) {
                    close();
                }
                return null;
            } else {
                return splitCells(line);
            }
        }

        /**
         * Read a logical CSV line, joining lines when they are divided
         * by a newline in quotes. If a complete CSV line could not be
         * read, and no exception was thrown, this method returns `null`.
         *
         * @return A (potentially multi-line) string representing a single
         * row from a CSV file (i.e. a "logical" CSV line).
         */
        private String readLogicalLine() throws IOException {
            StringBuilder acc = new StringBuilder();
            int quoteCount = 0;
            String line;

            if (!fileOpen) {
                return null;
            }

            // In a well-formed CSV file, there will always be an 
            // even number of quote characters iff the row is complete.
            // If the `quoteCount` is odd, the line is incomplete, so
            // read another line, append, count quote characters, and
            // add to total `quoteCount`. Repeat until `quoteCount` is even.
            while ((line = inputReader.readLine()) != null) {
                linecount += 1;
                acc.append(line);

                quoteCount += Util.count(line, '"');
                if (quoteCount % 2 == 0) {
                    break;
                }
            }

            if (line == null || quoteCount % 2 != 0) {
                return null;
            } else {
                String result = acc.toString();
                rowcount += 1;
                return result;
            }
        }

        private String[] splitCells(String line) {
            ArrayList<String> logicalCells = new ArrayList<String>();
            String[] cells = line.split(delim);
            String current = null;
            StringBuilder acc = new StringBuilder();
            int quoteCount = 0;

            // The logic above applies to quote counts in cells too!
            // So we do roughly the same thing with cells.
            for (int i = 0; i < cells.length; i++) {
                current = cells[i];
                acc.append(current);
                quoteCount += Util.count(current, '"');
                if (quoteCount % 2 == 0) {
                    current = trimQuotes(acc.toString());
                    current.replace("\"\"", "\"");
                    logicalCells.add(current);
                    acc.setLength(0);
                } else {
                    acc.append(delim);
                }
            }

            return logicalCells.toArray(new String[logicalCells.size()]);
        }

        private String trimQuotes(String s) {
            int start = 0;
            int end = s.length();

            // Remove outer quotation marks (if they are present) 
            // and any whitespace not contained between them. 
            s = s.replaceAll("^[\\s]*\"", "");
            s = s.replaceAll("\"[\\s]*$", "");
            return s;
        }
    }

    public static void main (String[] args) throws IOException {
        CsvReader csv = new CsvReader(args[0], ",", 1);
        for (String[] row : csv) {
            for (String cell : row) {
                System.out.print(cell);
                System.out.print(" | ");
            }
            System.out.println();
        }
    }
}
