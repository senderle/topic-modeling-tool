package cc.mallet.topics.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

    public int[][] buildNtd(int T, int D, String stateFile) throws IOException {
        int[][] Ntd = new int[T][D];
        try (BufferedReader in = 
                new BufferedReader(new FileReader(stateFile))) {
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

    public void topicWords(String topicKeysFile, String outputCsv) throws IOException {
        try (
            BufferedReader in =
                new BufferedReader(new FileReader(topicKeysFile));
            BufferedWriter out = 
                new BufferedWriter(new FileWriter(outputCsv))
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

    public String dtLine2Csv(String line) {
        String[] str = line.split(MALLET_CSV_DEL); // tab as split
        if (str.length >= 2) {
            docNames.add(str[1]);
            return Util.join(CSV_DEL, str);
        } else {
            return line;
        }
    }

    public String dtLine2dtMeta(String line) {
        return dtLine2dtMeta(line, "");
    }

    public String dtLine2dtMeta(String line, String metaLine) {
        String filename;
        String[] inCells = line.split(MALLET_CSV_DEL);
        String[] outCells = new String[numTopics + 1];
        Arrays.fill(outCells, "0.0");

        int topic;

        if (inCells.length >= 2) {
            docNames.add(inCells[1]);
            filename = Paths.get(inCells[1]).getFileName().toString();

            if (metaLine.equals("")) {
                outCells[0] = Util.join(CSV_DEL, inCells[0], filename);
            } else {
                outCells[0] = Util.join(CSV_DEL, inCells[0], filename, metaLine);
            }
            
            for (int i = 2; i < inCells.length - 1; i = i + 2) {
                topic = Integer.parseInt(inCells[i]);
                outCells[topic + 1] = inCells[i + 1];
            }

            return Util.join(CSV_DEL, outCells);
        } else {
            return line;
        }
    }

    public ArrayList<String> docTopicVectorsRow(String[] inCells) {
        ArrayList<String> outCells = 
            new ArrayList<String>(Collections.nCopies(numTopics, "0.0"));
        int topic;
        for (int i = 2; i < inCells.length - 1; i = i + 2) {
            topic = Integer.parseInt(inCells[i]);
            outCells.set(topic, inCells[i + 1]);
        }
        return outCells;
    }

    public HashMap<String, String[]> csvMap(Iterator<String[]> csvIterator)
    throws IOException {
        return csvMap(csvIterator, 0);
    }

    public HashMap<String, String[]> csvMap(
            Iterator<String[]> csvIterator, 
            int keyColumn
    ) throws IOException {
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

    public String topicHeader() {
        StringBuilder header = new StringBuilder();

        header.append(0);
        header.append(" ");
        header.append(Util.join(" ", topicHeaderWords.get(0)));
        for (int i = 1; i < topicHeaderWords.size(); i += 1) {
            header.append(CSV_DEL);
            header.append(i);
            header.append(" ");
            header.append(Util.join(" ", topicHeaderWords.get(i)));
        }

        return header.toString();
    }

    public ArrayList<String> topicHeaderCells() {
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

    public void topicsDocs(String docTopicsFile, String outputCsv) throws IOException {
        try (
            BufferedReader in = 
                new BufferedReader(new FileReader(docTopicsFile));
            BufferedWriter out = 
                new BufferedWriter(new FileWriter(outputCsv))
        ) {
            String line = null;
            int nd = 0;
            docNames = new ArrayList<String>();

            line = in.readLine();      //skip mallet header line

            String header = Util.join(CSV_DEL, "docId", "filename", "toptopics...");
            out.write(header + NEWLINE);
            while ((line = in.readLine()) != null) {
                nd++;
                out.write(dtLine2Csv(line) + NEWLINE);
            }
            out.flush();
            setNumDocs(nd);
        }
    }

    public void topicsVectors(String docTopicsFile, String outputCsv) throws IOException {  //topics in doc, as vectors
        try (
            BufferedReader in = 
                new BufferedReader(new FileReader(docTopicsFile));
            BufferedWriter out =
                new BufferedWriter(new FileWriter(outputCsv))
        ) {
            String line = null;
            int nd = 0;
            docNames = new ArrayList<String>();

            line = in.readLine();  // skip mallet header line

            String header = Util.join(CSV_DEL, "docId", "filename", topicHeader());
            out.write(header + NEWLINE);
            while ((line = in.readLine()) != null) {
                nd++;
                out.write(dtLine2dtMeta(line) + NEWLINE);
            }
            out.flush();
            setNumDocs(nd);
        }
    }

    public void topicsVectors(String docTopicsFile, String outputCsv, String metadataFile) throws IOException {  //topics in doc, as vectors
        try (
            BufferedReader in = 
                new BufferedReader(new FileReader(docTopicsFile));
            CsvWriter out = new CsvWriter(outputCsv);
        ) {
            CsvReader meta = new CsvReader(metadataFile, META_CSV_DEL, 1);
            ArrayList<String> cells;

            // Create new headers...
            //
            // skip MALLET header line
            in.readLine();      
            // Construct new metadata header.
            cells = new ArrayList<String>();
            // Start with MALLET headers
            cells.addAll(Arrays.asList("docId", "filename"));
            // Add headers from metadata.
            cells.addAll(Arrays.asList(meta.getHeaders().get(0)));
            // Add headers for topics (using headwords)
            cells.addAll(topicHeaderCells());
            out.writeCellRow(cells);

            // Everything else...
            writeTopicsVectorsRows(in, meta, out);
        }
    }

    private ArrayList<String> getEmptyMetaCells(int len) {
        ArrayList<String> emptyMetaCells = new ArrayList<String>(
            Collections.nCopies(len, "[empty]")
        );

        if (emptyMetaCells.size() > 0) {
            emptyMetaCells.set(0, "[filename-not-found-in-metadata]");
        }
        return emptyMetaCells;
    }


    public void writeTopicsVectorsRows(BufferedReader in, CsvReader meta, 
            CsvWriter out) 
    throws IOException {
        int nd = 0;
        String line, filename, malletId = null;
        HashMap<String, String[]> metaMap = null;
        List<String> cells, emptyMetaCells;

        // Create placeholder data of same length as metadata headers.
        emptyMetaCells = getEmptyMetaCells(meta.getHeaders().get(0).length);

        // Initialize document name storage to be filled below.
        docNames = new ArrayList<String>();

        // Create map from filenames to correct metadata.
        metaMap = csvMap(meta.iterator());

        // Use cells as a row accumulator.
        cells = new ArrayList<String>();
        while ((line = in.readLine()) != null) {
            nd++;

            String[] inLine = line.split(MALLET_CSV_DEL);
            if (inLine.length > 2) {
                malletId = inLine[0];
                filename = inLine[1];
                docNames.add(filename);
                
                filename = Paths.get(filename).getFileName().toString();
            } else {
                continue;
            }

            cells.clear();
            cells.addAll(Arrays.asList(malletId, filename));
            if (metaMap.containsKey(filename)) {
                cells.addAll(Arrays.asList(metaMap.get(filename))); 
            } else {
                cells.addAll(emptyMetaCells);
            }
            cells.addAll(docTopicVectorsRow(inLine));
            out.writeCellRow(cells);
        }
        setNumDocs(nd);
    }

    public void docsTopics(String stateFile, int numDocsShown, String outputCsv) throws IOException {
        Ntd =  buildNtd(numTopics, numDocs, stateFile);
        if (Ntd != null) {
            try (BufferedWriter out = 
                    new BufferedWriter(new FileWriter(outputCsv))) {
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

    public void setNumDocs(int value) {
        numDocs = value;
    }

    public void setNumTopics(int value) {
        numTopics = value;
    }

    public void createCsvFiles(String outputDir) throws IOException {
        createCsvFiles(outputDir, "");
    }

    public void createCsvFiles(String outputDir, String metadataFile) throws IOException {
        File csvDir = new File(outputDir + File.separator + "output_csv");    // TODO: replace all strings with constants
        csvDir.mkdir();
        String csvDirPath = csvDir.getAbsolutePath();

        topicWords(outputDir + File.separator + "output_topic_keys", 
                csvDirPath + File.separator + TOPIC_WORDS);
        topicsDocs(outputDir + File.separator + "output_doc_topics.txt", 
                csvDirPath + File.separator + TOPICS_IN_DOCS);

        if (metadataFile.equals("")) {
            topicsVectors(outputDir + File.separator + "output_doc_topics.txt", 
                    csvDirPath + File.separator + TOPICS_IN_DOCS_VECTORS);
        } else {
            topicsVectors(outputDir + File.separator + "output_doc_topics.txt", 
                    csvDirPath + File.separator + TOPICS_IN_DOCS_VECTORS, metadataFile);
        }
        
        docsTopics(outputDir + File.separator + "output_state", 
                Math.min(500, numDocs), csvDirPath + File.separator + DOCS_IN_TOPICS);
    }

    public int[][] getNtd() {
        return Ntd;
    }

}
