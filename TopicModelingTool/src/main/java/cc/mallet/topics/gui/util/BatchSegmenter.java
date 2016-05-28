package cc.mallet.topics.gui.util;

import cc.mallet.topics.gui.util.CsvReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.util.ArrayList;

public class BatchSegmenter {
    private String inputDir = null;
    private String outSuffix = null;
    private String[] oldHeader = null;
    private CsvReader oldMetadata = null;
    private ArrayList<String[]> newMetadata = null;

    public BatchSegmenter(String inputDir, String metadata, 
            String metadataDelim, String outSuffix) {
        this.inputDir = inputDir;
        this.outSuffix = outSuffix;
        this.oldMetadata = new CsvReader(metadata, metadataDelim);
    }

    public BatchSegmenter(String inputDir, String metadata, 
            String metadataDelim) {
        this(inputDir, metadata, metadataDelim, "_chunks");
    }

    public BatchSegmenter(String inputDir, String metadata) {
        this(inputDir, metadata, ",", "_chunks");
    }

    public String[] segment() {
        String filename;
        String[] res = {"foo", "bar"};
        for (String[] row : oldMetadata) {
            if (row.length > 0) {
                filename = row[0];
                System.out.println(Files.exists(Paths.get(filename)));
            }

            res = row;
        }
        return res;
    }

    // Constructor takes input directory, output directory suffix, metadata

    // A method that takes a metadata row with a filename, 
    // creates a FileSplitter, grabs chunks and word counts, saves the chunks 
    // in new files based on a filename-n pattern in a folder using a 
    // predefined suffix constant ("_chunks"), and for each saved file, 
    // creates a new metadata row with the new filename and an extra row
    // containing the file wordcount, and appends it to an accumulator
    // attribute. 
    // 
    // A method that takes a list of metadata rows and does the above for
    // each. (STRING[][] -> null (with side effects))
    //
    // A method that creates new filenames based on dir-suffix, 
    // original-filename arguments (DIRSUFFIX, FILENAME, FILE_N) -> FILENAME
    //
    // a method that transforms an old metadata row into a new metadata
    // row, where metadata rows are lists of lists of strings, and the first
    // value in each list is assumed to be the filename. possibly make this
    // accept some more canonical form based on what I used in the CSV class;
    // I don't remember right now. The filename is transformed by the above
    // method and replaces the old filename; the list is extended by one
    // with the word count information. (METADATA_ROW, WORD_COUNT, 
    // FILE_N) -> METADATA_ROW
    //
    //
    //

    public static void main(String[] args) throws IOException {
        BatchSegmenter bs = new BatchSegmenter(args[0], args[1]);
        bs.segment();
    }
}
