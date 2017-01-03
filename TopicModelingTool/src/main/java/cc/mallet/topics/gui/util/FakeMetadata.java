package cc.mallet.topics.gui.util;

import cc.mallet.topics.gui.util.CsvWriter;

import java.util.List;
import java.util.ArrayList;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.io.IOException;


public class FakeMetadata {
    private Path inputDir = null;
    private Path outputFile = null;
    private String delim = null;

    public FakeMetadata(String inputDir, String outputFile, String delim) {
        this.inputDir = Paths.get(inputDir);
        this.outputFile = Paths.get(outputFile);
        this.delim = delim;
    }

    public FakeMetadata(Path inputDir, Path outputFile, String delim) {
        this.inputDir = inputDir;
        this.outputFile = outputFile;
        this.delim = delim;
    }

    private static List<String[]> fileList(String directory) {
        return fileList(Paths.get(directory));
    }

    private static List<String[]> fileList(Path directory) {
        List<String[]> filenames = new ArrayList<>();
        String[] row = {"filename"};
        filenames.add(row);
        row[0] = "";
        try (DirectoryStream<Path> directoryStream = 
                Files.newDirectoryStream(directory)) {
            for (Path path : directoryStream) {
                row = new String[1];
                row[0] = path.getFileName().toString();
                filenames.add(row);
            }
        } catch (IOException exc) {
            return filenames;
        }

        return filenames;
    }

    private void writeAll() {
        try (CsvWriter csv = new CsvWriter(outputFile, delim)) {
            csv.writeRows(fileList(inputDir));
        }
    }

    public static void write(String inp, String outp, String del) {
        FakeMetadata md = new FakeMetadata(inp, outp, del);
        md.writeAll();
    }

    public static void write(Path inp, Path outp, String del) {
        FakeMetadata md = new FakeMetadata(inp, outp, del);
        md.writeAll();
    }
}
