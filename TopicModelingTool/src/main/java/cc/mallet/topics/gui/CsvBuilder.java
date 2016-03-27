package cc.mallet.topics.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Comparator;
import java.util.logging.Logger;
import static cc.mallet.topics.gui.TopicModelingTool.MALLET_CSV_DEL;
import static cc.mallet.topics.gui.TopicModelingTool.NEWLINE;
import static cc.mallet.topics.gui.TopicModelingTool.TOPIC_WORDS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS_VECTORS;
import static cc.mallet.topics.gui.TopicModelingTool.DOCS_IN_TOPICS;


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

    public int[][] buildNtd(int T, int D, String stateFile) throws java.io.IOException {
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

    public String join(String delim, String... cells) {
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

    public void topicWords(String topicKeysFile, String outputCsv) throws java.io.IOException {
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
                outrow.append(join(" ", words));
                outrow.append(NEWLINE);
                out.write(outrow.toString());
            }
        }
    }

    public String dtLine2Csv(String line) {
        String[] str = line.split(MALLET_CSV_DEL); // tab as split
        if (str.length >= 2) {
            docNames.add(str[1]);
            return join(CSV_DEL, str);
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
                outCells[0] = join(CSV_DEL, inCells[0], filename);
            } else {
                outCells[0] = join(CSV_DEL, inCells[0], filename, metaLine);
            }
            
            for (int i = 2; i < inCells.length - 1; i = i + 2) {
                topic = Integer.parseInt(inCells[i]);
                outCells[topic + 1] = inCells[i + 1];
            }

            return join(CSV_DEL, outCells);
        } else {
            return line;
        }
    }

    public HashMap<String, String[]> csvMap(
            BufferedReader openCsv, 
            String delim
    ) throws java.io.IOException {
        return csvMap(openCsv, delim, 0);
    }

    public HashMap<String, String[]> csvMap(
            BufferedReader openCsv, 
            String delim,
            int keyColumn
    ) throws java.io.IOException {
        String csvLine;
        String[] csvCells;
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        while ((csvLine = openCsv.readLine()) != null) {
            csvCells = csvLine.split(delim);
            if (csvCells.length <= keyColumn) {
                log.warning("csvMap: keyColumn out of bounds. Bad line:");
                log.warning(csvLine);
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
        header.append(join(" ", topicHeaderWords.get(0)));
        for (int i = 1; i < topicHeaderWords.size(); i += 1) {
            header.append(CSV_DEL);
            header.append(i);
            header.append(" ");
            header.append(join(" ", topicHeaderWords.get(i)));
        }

        return header.toString();
    }

    public void topicsDocs(String docTopicsFile, String outputCsv) throws java.io.IOException {
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

            String header = join(CSV_DEL, "docId", "filename", "toptopics...");
            out.write(header + NEWLINE);
            while ((line = in.readLine()) != null) {
                nd++;
                out.write(dtLine2Csv(line) + NEWLINE);
            }
            out.flush();
            setNumDocs(nd);
        }
    }

    public void topicsVectors(String docTopicsFile, String outputCsv) throws java.io.IOException {  //topics in doc, as vectors
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

            String header = join(CSV_DEL, "docId", "filename", topicHeader());
            out.write(header + NEWLINE);
            while ((line = in.readLine()) != null) {
                nd++;
                out.write(dtLine2dtMeta(line) + NEWLINE);
            }
            out.flush();
            setNumDocs(nd);
        }
    }

    public void topicsVectors(String docTopicsFile, String outputCsv, String metadataFile) throws java.io.IOException {  //topics in doc, as vectors
        try (
            BufferedReader in = 
                new BufferedReader(new FileReader(docTopicsFile));
            BufferedReader meta = 
                new BufferedReader(new FileReader(metadataFile));
            BufferedWriter out =
                new BufferedWriter(new FileWriter(outputCsv))
        ) {
            int nd = 0;
            String line, filename, metaLine = null;
            String[] metaHeader = null;
            String[] metaEmpty = null;
            HashMap<String, String[]> metaMap = null;

            // Initialize instance variable; will be filled in dtLine2dtMeta.
            docNames = new ArrayList<String>();

            line = in.readLine();      //skip MALLET header line
            metaHeader = meta.readLine().split(META_CSV_DEL);
            metaLine = join(CSV_DEL, metaHeader);
            metaLine = join(CSV_DEL, "docId", "filename", 
                    metaLine, topicHeader());
            out.write(metaLine + NEWLINE);
           
            metaEmpty = new String[metaHeader.length];
            Arrays.fill(metaEmpty, "[data-missing]");

            metaMap = csvMap(meta, META_CSV_DEL);
            while ((line = in.readLine()) != null) {
                nd++;
                // Then, here, call a new function on line that pulls the
                // filename, looks it up in the HashMap, and returns the 
                // metaLine

                filename = line.split(MALLET_CSV_DEL)[1];
                filename = Paths.get(filename).getFileName().toString();

                if (metaMap.containsKey(filename)) {
                    metaLine = join(CSV_DEL, metaMap.get(filename));
                } else {
                    metaLine = join(CSV_DEL, metaEmpty);
                }
                out.write(dtLine2dtMeta(line, metaLine) + CSV_DEL + NEWLINE);
            }
            out.flush();
            setNumDocs(nd);
        }
    }

    public void docsTopics(String stateFile, int numDocsShown, String outputCsv) throws java.io.IOException {
        Ntd =  buildNtd(numTopics, numDocs, stateFile);
        if (Ntd != null) {
            try (BufferedWriter out = 
                    new BufferedWriter(new FileWriter(outputCsv))) {
                String header = join(CSV_DEL, "topicId", "rank", 
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

    public void createCsvFiles(String outputDir) throws java.io.IOException {
        createCsvFiles(outputDir, "");
    }

    public void createCsvFiles(String outputDir, String metadataFile) throws java.io.IOException {
        File csvDir = new File(outputDir + File.separator + "output_csv");    // TODO: replace all strings with constants
        csvDir.mkdir();
        String csvDirPath = csvDir.getPath();

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
