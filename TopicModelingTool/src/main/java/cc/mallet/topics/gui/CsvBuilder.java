package cc.mallet.topics.gui;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Closeable;
import java.io.IOException;

import java.nio.file.Paths;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Collections;
import java.util.logging.Logger;

import static cc.mallet.topics.gui.TopicModelingTool.MALLET_CSV_DEL;
import static cc.mallet.topics.gui.TopicModelingTool.NEWLINE;
import static cc.mallet.topics.gui.TopicModelingTool.TOPIC_WORDS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS_VECTORS;
import static cc.mallet.topics.gui.TopicModelingTool.DOCS_IN_TOPICS;

import static cc.mallet.topics.gui.TopicModelingTool.MALLET_TOPIC_KEYS;
import static cc.mallet.topics.gui.TopicModelingTool.MALLET_STATE;
import static cc.mallet.topics.gui.TopicModelingTool.MALLET_DOC_TOPICS;

import static cc.mallet.topics.gui.TopicModelingTool.MALLET_OUT;
import static cc.mallet.topics.gui.TopicModelingTool.CSV_OUT;
import static cc.mallet.topics.gui.TopicModelingTool.HTML_OUT;

import cc.mallet.topics.gui.util.Util;
import cc.mallet.topics.gui.util.CsvReader;
import cc.mallet.topics.gui.util.CsvWriter;
public class CsvBuilder {

    private final static Logger log =
        Logger.getLogger(CsvBuilder.class.getName());

    String META_CSV_DEL = ",";
    String CSV_DEL = ",";
    int numDocs;
    int numTopics;
    String START_DOC_ID = "0";
    ArrayList<String> docNames; // Potential memory issue for very large collections.
    int[][] Ntd;

    ArrayList<String[]> topicHeaderWords;

    public CsvBuilder(int numTopics) {
        this(numTopics, ",", ",");
    }

    public CsvBuilder(int numTopics, String metaDelim) {
        this(numTopics, metaDelim, ",");
    }

    public CsvBuilder(int topics, String metaDelim, String csvDelim) {
        numTopics = topics;
        META_CSV_DEL = metaDelim;
        CSV_DEL = csvDelim;
    }

    private Integer[] sortTopicIdx(final int[] docScores) {
        final Integer[] idx = new Integer[numDocs];
        for(int i = 0; i < numDocs; i++){
            idx[i] = i;
        }

        Arrays.sort(idx, new Comparator<Integer>() {
            @Override public int compare(final Integer o1, final Integer o2) {
                return docScores[o1] - docScores[o2];
            }
        });

        return idx;
    }

    public int[][] buildNtd(int T, int D, String stateFile) throws IOException {
        int[][] Ntd = new int[T][D];
        try (
                BufferedReader in = Files.newBufferedReader(
                    Paths.get(stateFile),
                    Charset.forName("UTF-8")
                )
        ) {
            String line = null;

            in.readLine(); in.readLine(); in.readLine();      // stateFile has three header lines
            String curDocId = START_DOC_ID;
            int curDocIndex = 0;
            while ((line = in.readLine()) != null) {
                String[] strArr= line.split(" ");
                if (!strArr[0].equals(curDocId)) {
                    curDocIndex++;
                    curDocId = strArr[0];
                }
                int wordTopicIndex = Integer.parseInt(strArr[strArr.length - 1]);
                Ntd[wordTopicIndex][curDocIndex]++;
            }

            return Ntd;
        }
    }

    public void docsTopics(
            String stateFile,
            int numDocsShown,
            String outputCsv
    ) throws IOException {
        Ntd =  buildNtd(numTopics, numDocs, stateFile);
        if (Ntd != null) {
            try (
                    BufferedWriter out = Files.newBufferedWriter(
                        Paths.get(outputCsv),
                        Charset.forName("UTF-8")
                    )
            ) {
                String header = Util.join(CSV_DEL, "topicId", "rank",
                        "docId", "filename");
                out.write(header + NEWLINE);
                String line;
                for (int i = 0; i < numTopics; i++){
                    Integer[] idx = sortTopicIdx(Ntd[i]);
                    for (int j = 0; j < numDocsShown; j++) {
                        int k = idx[numDocs - j - 1];
                        line = i + CSV_DEL + j + CSV_DEL + k + CSV_DEL + docNames.get(k) + NEWLINE;
                        out.write(line);
                    }
                }
                out.flush();
            }
        } else {
            System.out.println("NTB is NULL!!!");
        }
    }

    public void topicWords(String topicKeysFile, String outputCsv)
    throws IOException {
        try (
                BufferedReader in = Files.newBufferedReader(
                    Paths.get(topicKeysFile),
                    Charset.forName("UTF-8")
                 );
                 BufferedWriter out = Files.newBufferedWriter(
                    Paths.get(outputCsv),
                    Charset.forName("UTF-8")
                 );
        ) {
            out.write("Topic Id" + CSV_DEL + "Top Words..." + NEWLINE);

            topicHeaderWords = new ArrayList<String[]>();
            String[] fields;
            String[] words;
            String line;

            StringBuilder outrow;

            while ((line = in.readLine()) != null) {
                fields = line.split(MALLET_CSV_DEL);
                words = fields[2].split(" ");

                // Just 3 headwords for now, hardcoded.
                topicHeaderWords.add(Arrays.copyOfRange(words, 0, 3));

                outrow = new StringBuilder();
                outrow.append(fields[0]);
                outrow.append(CSV_DEL);
                outrow.append(Util.join(" ", words));
                outrow.append(NEWLINE);
                out.write(outrow.toString());
            }
        }
    }

    public void topicsDocs(String docTopicsFile, String outputCsv)
    throws IOException {
        try (
                BufferedReader in = Files.newBufferedReader(
                    Paths.get(docTopicsFile),
                    Charset.forName("UTF-8")
                 );
                BufferedWriter out = Files.newBufferedWriter(
                    Paths.get(outputCsv),
                    Charset.forName("UTF-8")
                );
        ) {
            String line = null;
            String[] row = null;
            int nd = 0;
            docNames = new ArrayList<String>();
            String header = Util.join(CSV_DEL, "docId", "filename", "toptopics...");

            line = in.readLine();      //skip mallet header line

            out.write(header + NEWLINE);
            while ((line = in.readLine()) != null) {
                nd++;
                row = line.split(MALLET_CSV_DEL);
                if (row.length >= 2) {
                    docNames.add(row[1]);
                    line = Util.join(CSV_DEL, row);
                }
                out.write(line + NEWLINE);
            }
            out.flush();
            setNumDocs(nd);
        }
    }

    public void topicsVectors(
            String docTopicsFile,
            String outputCsv
    ) throws IOException {
        topicsVectors(docTopicsFile, outputCsv, null);
    }

    public void topicsVectors(
            String docTopicsFile,
            String outputCsv,
            String metadataFile
    ) throws IOException {
        try (
                BufferedReader in = Files.newBufferedReader(
                    Paths.get(docTopicsFile),
                    Charset.forName("UTF-8")
                 );
                CsvWriter out = new CsvWriter(outputCsv);
        ) {
            CsvReader meta = null;
            if (metadataFile != null) {
                meta = new CsvReader(metadataFile, META_CSV_DEL, 1);
            }

            // Skip MALLET header line.
            in.readLine();

            // Concatenate MALLET, Metadata, and Topic headers:
            ArrayList<String> cells = new ArrayList<String>();
            cells.addAll(Arrays.asList("docId", "filename"));
            cells.addAll(metadataHeaderCells(meta));
            cells.addAll(topicHeaderCells());
            out.writeCellRow(cells);

            writeTopicsVectorsRows(in, meta, out);
        }
    }

    private void writeTopicsVectorsRows(
            BufferedReader in,
            CsvReader meta,
            CsvWriter out
    ) throws IOException {
        int nd = 0;
        int nheaders = 0;
        String line, filename, malletId = null;
        HashMap<String, String[]> metaMap = null;
        List<String> cells, emptyMetaCells = null;

        // Create placeholder data of same length as metadata headers.
        if (meta != null) {
            nheaders = meta.getHeaders().get(0).length;
        }

        emptyMetaCells = getEmptyMetaCells(nheaders);

        // Initialize document name storage to be filled below.
        docNames = new ArrayList<String>();

        // Create map from filenames to correct metadata.
        metaMap = csvMap(meta);

        // Use cells as a row accumulator.
        cells = new ArrayList<String>();
        while ((line = in.readLine()) != null) {
            nd++;

            String[] inLine = line.split(MALLET_CSV_DEL);
            if (inLine.length > 2) {
                malletId = inLine[0];
                filename = inLine[1];
                docNames.add(filename);

                filename = Paths.get(java.net.URI.create(filename)).getFileName().toString();
            } else {
                continue;
            }

            cells.clear();
            cells.addAll(Arrays.asList(malletId, filename));
            cells.addAll(metadataRowCells(metaMap, filename, emptyMetaCells));
            cells.addAll(topicRowCells(inLine));
            out.writeCellRow(cells);
        }
        setNumDocs(nd);
    }

    private ArrayList<String> topicHeaderCells() {
        ArrayList<String> header = new ArrayList<String>();
        StringBuilder headerCell = new StringBuilder();

        for (int i = 0; i < topicHeaderWords.size(); i += 1) {
            headerCell.append(i);
            headerCell.append(" ");
            headerCell.append(Util.join(" ", topicHeaderWords.get(i)));
            header.add(headerCell.toString());
            headerCell.setLength(0);
        }

        return header;
    }

    private List<String> metadataHeaderCells(CsvReader meta)
    throws IOException {
        if (meta != null) {
            return Arrays.asList(meta.getHeaders().get(0));
        } else {
            return new ArrayList<String>();
        }
    }

    private ArrayList<String> topicRowCells(String[] inCells) {
        ArrayList<String> outCells =
            new ArrayList<String>(Collections.nCopies(numTopics, "0.0"));
        int topic;
        for (int i = 2; i < inCells.length - 1; i = i + 2) {
            topic = Integer.parseInt(inCells[i]);
            outCells.set(topic, inCells[i + 1]);
        }
        return outCells;
    }

    private List<String> metadataRowCells(
            HashMap<String, String[]> meta,
            String filename,
            List<String> fallback
    ) {
        if (meta == null) {
            return new ArrayList<String>();
        } else if (meta.containsKey(filename)) {
            return Arrays.asList(meta.get(filename));
        } else {
            return fallback;
        }
    }

    private HashMap<String, String[]> csvMap(Iterable<String[]> csvIterable)
    throws IOException {
        return csvMap(csvIterable, 0);
    }

    private HashMap<String, String[]> csvMap(
            Iterable<String[]> csvIterable,
            int keyColumn
    ) throws IOException {
        if (csvIterable == null) {
            return null;
        }

        Iterator<String[]> csvIterator = csvIterable.iterator();

        String[] csvCells;
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        while ((csvCells = csvIterator.next()) != null) {
            if (csvCells.length <= keyColumn) {
                log.warning("csvMap: keyColumn out of bounds. Bad line:");
                log.warning(Util.join(META_CSV_DEL, csvCells));
            } else {
                map.put(csvCells[keyColumn], csvCells);
            }
        }
        return map;
    }

    private ArrayList<String> getEmptyMetaCells(int len) {
        ArrayList<String> emptyMetaCells = new ArrayList<String>(
            Collections.nCopies(len, "[missing metadata]")
        );

        if (emptyMetaCells.size() > 0) {
            emptyMetaCells.set(0, "[filename not found in metadata]");
        }
        return emptyMetaCells;
    }

    public void setNumDocs(int value) {
        numDocs = value;
    }

    public void setNumTopics(int value) {
        numTopics = value;
    }

    public void createCsvFiles(String outputDir) throws IOException {
        createCsvFiles(outputDir, "");
    }

    public void createCsvFiles(String outputDir, String metadataFile)
    throws IOException {
        File csvDir = new File(outputDir + File.separator + CSV_OUT);
		File malletDir = new File(outputDir + File.separator + MALLET_OUT);
        String csvDirPath = csvDir.getAbsolutePath();
		String malletDirPath = malletDir.getAbsolutePath();

        topicWords(malletDirPath + File.separator + MALLET_TOPIC_KEYS,
                csvDirPath + File.separator + TOPIC_WORDS);
        topicsDocs(malletDirPath + File.separator + MALLET_DOC_TOPICS,
                csvDirPath + File.separator + TOPICS_IN_DOCS);

        if (metadataFile.equals("")) {
            topicsVectors(malletDirPath + File.separator + MALLET_DOC_TOPICS,
                    csvDirPath + File.separator + TOPICS_IN_DOCS_VECTORS);
        } else {
            topicsVectors(malletDirPath + File.separator + MALLET_DOC_TOPICS,
                    csvDirPath + File.separator + TOPICS_IN_DOCS_VECTORS, metadataFile);
        }

        docsTopics(malletDirPath + File.separator + MALLET_STATE,
                Math.min(500, numDocs), csvDirPath + File.separator + DOCS_IN_TOPICS);
    }

    public int[][] getNtd() {
        return Ntd;
    }

}
