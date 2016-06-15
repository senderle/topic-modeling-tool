package cc.mallet.topics.gui.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.Closeable;

public class FileSplitter implements Closeable {
    private Path inputPath = null;
    private BufferedReader inputReader = null;
    private int wordsRead = 0;
    private int segmentsRead = 0;
    private String currentLine = null;
 
    public FileSplitter(Path path) {
        inputPath = path;

        try {
            inputReader = Files.newBufferedReader(inputPath);
        } catch (IOException exc) {
            System.out.println(inputPath.toString() + ": Error reading file");
            throw new RuntimeException(exc);
        }
    }

    public FileSplitter(String path) {
        this(Paths.get(path));
    }

    public FileSplitter(File path) {
        this(path.toPath());
    }

    public String readSegment(int nwords) throws IOException {
        int endWord = wordsRead + nwords;
        StringBuilder out = new StringBuilder();
        StringBuilder remain = new StringBuilder();

        if (currentLine == null) {
            currentLine = inputReader.readLine();
        }

        while (currentLine != null && wordsRead < endWord) {
            String[] words = currentLine.split("\\s");
            for (String w : words) {
                if (wordsRead < endWord) {
                    out.append(w);
                    out.append(" ");
                    wordsRead += 1;
                } else {
                    remain.append(w);
                    remain.append(" ");
                }
            }

            if (remain.length() > 0) {
                currentLine = remain.toString();
            } else {
                currentLine = inputReader.readLine();
            }
        }

        if (out.length() < 1) { 
            return null;
        } else {
            segmentsRead += 1;
            return out.toString();
        }
	}

    public String getSegment(int nwords) {
        try {
            return readSegment(nwords);
        } catch (IOException exc) {
            return null;
        }
    }

    public int getWordsRead() {
        return wordsRead;
    }

    public int getSegmentsRead() {
        return segmentsRead;
    }

    public void close() throws IOException {
        inputReader.close();
    }

    public static void main (String[] args) throws IOException {
        int segs = Integer.parseInt(args[0]);
        try (FileSplitter sp = new FileSplitter(args[1])) {
            for (String seg = sp.getSegment(segs); seg != null; seg = sp.getSegment(segs)) {
                System.out.println(seg);
                System.out.println(sp.getWordsRead());
            }
        }
    }
}
