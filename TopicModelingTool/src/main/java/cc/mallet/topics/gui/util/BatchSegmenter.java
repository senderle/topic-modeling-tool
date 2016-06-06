package cc.mallet.topics.gui.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

public class BatchSegmenter {
    private Path inputDir = null;
    private Path segmentDir = null;
    private CsvReader oldMetadata = null;
    private ArrayList<String[]> newMetadata = null;

    public BatchSegmenter(String metadata, String inputDir,
            String segmentDir, String metadataDelim) {
        this.inputDir = Paths.get(inputDir);
        this.segmentDir = Paths.get(segmentDir);
        this.oldMetadata = new CsvReader(metadata, metadataDelim);
        this.newMetadata = new ArrayList<String[]>();
    }

    public BatchSegmenter(String metadata, String inputDir, String segmentDir) {
        this(metadata, inputDir, segmentDir, ",");
    }

    public ArrayList<String[]> segment(int nsegs) throws IOException {
        String filename;
        ArrayList<String[]> res = null;
        for (String[] row : oldMetadata) {
            if (row.length > 0) {
                res = metaSegment(row, nsegs);
            }
            newMetadata.addAll(res);
        }
        return newMetadata;
    }

    /**
     * Append the given segment number onto the given filename, using
     * a `'-'` as the joining character. When the filename ends with
     * an extension (i.e. a suffix beginning with a `'.'` character),
     * this inserts the segment number between the base name and the
     * extension. This is a purely aesthetic convenience, since there 
     * is no well defined notion of "file extension". To be precise, 
     * there's no way to distinguish between `"filenames.with.many.dots"`
     * and `"filenames_with_multidot_extensions.tar.gz"`. So `".tar.gz"`
     * will become `".tar-1.gz"`, and that's just the way the cookie 
     * crumbles.
     *
     * If somebody in the Java language dev world were to take the 
     * trouble, I'm sure some sensible defaults could handle common 
     * special cases, like `".tar.gz"`. That would be great! But
     * the people in charge would have to actually do it; there's no
     * point in writing something like that if it's not incorporated
     * into the language. And incorporating such a thing into the
     * language would involve a tremendous amount of bickering and
     * "stakeholder" consultation. This is why languages designed 
     * by committees have such terrible standard libraries.
     *
     * The upshot of all this is that there is no built-in way
     * to split an extension, and now you have to read a weird
     * DIY regex if you want to know exactly how this works.
     *
     * Sorry. /rant
     */
    private String genSegmentName(String name, int segnum) {
        if (name.matches("^.*\\.[^\\.]+$")) {
            // "{filename}.{ext}" -> "{filename}-{segNum}.{ext}"
            name = name.replaceAll("(^.*)(\\.[^\\.]*$)", 
                                   "$1-" + 
                                   Integer.toString(segnum) + 
                                   "$2");
        } else {
            // "{filename}" -> "{filename}-{segnum}"
            name = name + "-" + Integer.toString(segnum);
        }

        return name;
    }

    private ArrayList<String[]> metaSegment(String[] metarow, int nsegs) throws IOException {
        String metaFilename = metarow[0];
        Path inputFile = inputDir.resolve(Paths.get(metaFilename));
        
        ArrayList<String[]> newrows = new ArrayList<String[]>();
        
        if ( ! Files.exists(inputFile)) {
            return newrows;
        }

        try (FileSplitter sp = new FileSplitter(inputFile)) {
            int wordsRead = 0;
            for (String seg = sp.getSegment(nsegs); 
                    seg != null; 
                    seg = sp.getSegment(nsegs)) {

                String[] newrow = Arrays.copyOf(metarow, metarow.length + 1);
                String segmentFilename = 
                    genSegmentName(newrow[0], sp.getSegmentsRead());

                newrow[0] = segmentFilename;
                newrow[newrow.length - 1] = 
                    Integer.toString(sp.getWordsRead() - wordsRead);
                wordsRead = sp.getWordsRead();
                newrows.add(newrow);

                Path outputFile = segmentDir.resolve(segmentFilename);
                System.out.println(outputFile.toString());

                ArrayList<String> outLine = new ArrayList<String>();
                outLine.add(seg);
                Files.write(outputFile, outLine);
            }
        }

        return newrows;
    }

    public static void main(String[] args) throws IOException {
        BatchSegmenter bs = new BatchSegmenter(args[0], args[1], args[2]);
        for (String[] row : bs.segment(5)) {
            System.out.println(Arrays.toString(row));
        }
    }
}
