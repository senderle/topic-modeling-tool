package cc.mallet.topics.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;
import static cc.mallet.topics.gui.TopicModelingTool.CSV_DEL;
import static cc.mallet.topics.gui.TopicModelingTool.TOPIC_WORDS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS_VECTORS;
import static cc.mallet.topics.gui.TopicModelingTool.DOCS_IN_TOPICS;


public class CsvBuilder {

    private final static Logger log = 
        Logger.getLogger(CsvBuilder.class.getName());
    public static final String META_CSV_DEL = ",";

    int numDocs;
    int numTopics;
    String START_DOC_ID = "0";
    ArrayList<String> docNames; // Potential memory issue for very large collections.
    int[][] Ntd;

    ArrayList<String[]> topicHeaderWords;

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

    public String join(String delim, String[] cells) {
        StringBuilder row = new StringBuilder();
        if (cells.length > 0) {
            row.append(cells[0]);
        }

        for (int i = 1; i < cells.length; i += 1) {
            row.append(delim);
            row.append(cells[i]);
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
            out.write("Topic Id" + CSV_DEL + "Top Words..." + "\n");
           
            topicHeaderWords = new ArrayList<String[]>();
            String[] fields;
            String[] words;
            String line;

            StringBuilder outrow;

            while ((line = in.readLine()) != null) {
                fields = line.split("\\t");
                words = fields[2].split(" ");
               
                // Just 3 headwords for now, hardcoded.
                topicHeaderWords.add(Arrays.copyOfRange(words, 0, 3));

                outrow = new StringBuilder();
                outrow.append(fields[0]);
                outrow.append(CSV_DEL);
                outrow.append(join(" ", words));
                outrow.append("\n");
                out.write(outrow.toString());
            }
        }
    }

    public String dtLine2Csv(String line) {
        String[] str = line.split("\\t"); // tab as split
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
        String[] inCells = line.split("\\t");
        String[] outCells = new String[numTopics + 3];
        Arrays.fill(outCells, "0.0");

        int topic;

        if (inCells.length >= 2) {
            outCells[0] = inCells[0];
            outCells[1] = inCells[1];
            outCells[2] = metaLine;
            for (int i = 2; i < inCells.length - 1; i = i + 2) {
                topic = Integer.parseInt(inCells[i]);
                outCells[topic + 3] = inCells[i + 1];
            }

            docNames.add(inCells[1]);
            return join(CSV_DEL, outCells);
        } else {
            return line;
        }
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

            String header = "docId" + CSV_DEL + "filename" + CSV_DEL + "toptopics...";            //variable number of topics for each doc
            out.write(header + "\n");
            while ((line = in.readLine()) != null) {
                nd++;
                out.write(dtLine2Csv(line) + "\n");
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

            line = in.readLine();      //skip mallet header line
            
            String header = "docId" + CSV_DEL + "filename" + CSV_DEL + topicHeader(); 
            out.write(header + "\n");
            while ((line = in.readLine()) != null) {
                nd++;
                out.write(dtLine2dtMeta(line) + "\n");
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
            String line = null;
            String metaLine = null;
            int nd = 0;
            docNames = new ArrayList<String>();

            line = in.readLine();      //skip mallet header line
            metaLine = join(CSV_DEL, meta.readLine().split(META_CSV_DEL));
            
            String header = "docId" + CSV_DEL + "filename" + CSV_DEL + metaLine + CSV_DEL + topicHeader();
            out.write(header + "\n");
            while ((line = in.readLine()) != null && (metaLine = meta.readLine()) != null) {
                nd++;
                out.write(dtLine2dtMeta(line, metaLine) + CSV_DEL + "\n");
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
                String header = "topicId" + CSV_DEL + "rank" + CSV_DEL + "docId" + CSV_DEL + "filename";
                out.write(header + "\n");
                String line;
                for (int i = 0; i < numTopics; i++){
                    Integer[] idx = sortTopicIdx(Ntd[i]);
                    for (int j = 0; j < numDocsShown; j++) {
                        int k = idx[numDocs - j - 1];
                        line = i + CSV_DEL + j + CSV_DEL + k + CSV_DEL + docNames.get(k) + "\n";
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

    public void createCsvFiles(int numTopics, String outputDir) throws java.io.IOException {
        createCsvFiles(numTopics, outputDir, "");
    }

    public void createCsvFiles(int numTopics, String outputDir, String metadataFile) throws java.io.IOException {
        File csvDir = new File(outputDir + File.separator+ "output_csv");    // TODO: replace all strings with constants
        csvDir.mkdir();
        setNumTopics(numTopics);
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

    public static void main(String[] args) {
        // TODO: Auto-generated method stub
        CsvBuilder o = new CsvBuilder();
    }
}
