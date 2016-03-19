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
import static cc.mallet.topics.gui.TopicModelingTool.CSV_DEL;
import static cc.mallet.topics.gui.TopicModelingTool.TOPIC_WORDS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS;
import static cc.mallet.topics.gui.TopicModelingTool.TOPICS_IN_DOCS_VECTORS;
import static cc.mallet.topics.gui.TopicModelingTool.DOCS_IN_TOPICS;

public class CsvBuilder {

    int numDocs;
    int numTopics;
    String START_DOC_ID = "0"; //FIXME
    ArrayList<String> docNames ;            //May fail for LARGE collections
    int[][] Ntd;


    public void buildCsv1(String topicKeysFile, String outputCsv)
    {
        try
        {
            FileReader fread = new FileReader(topicKeysFile);
            BufferedReader in = new BufferedReader(fread);
            FileWriter fwrite = new FileWriter(outputCsv);
            BufferedWriter out = new BufferedWriter(fwrite);
            String header = "topicId" + CSV_DEL + "words..";
            out.write(header + "\n");
            String line;

            while ((line = in.readLine()) != null)
            {
                String[] strArr = line.split("\\t| ");
                line = strArr[0] + CSV_DEL + strArr[2];
                for (int i=3; i<strArr.length; i++)
                {
                    line = line + " " + strArr[i];
                }
                out.write(line + "\n");
            }
            out.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public int[][] buildNtd(int T, int D, String stateFile)
    {
        int[][] Ntd = new int[T][D];
        try
        {
            FileReader fread = new FileReader(stateFile);
            BufferedReader in = new BufferedReader(fread);
            String line = null;

            in.readLine(); in.readLine(); in.readLine();      //header lines
            String curDocId = START_DOC_ID;    //MAY BREAK
            int curDocIndex = 0;
            while ((line = in.readLine()) != null){
                String[] strArr= line.split(" ");
                if(!strArr[0].equals(curDocId)){
                    curDocIndex++;
                    curDocId = strArr[0];
                }
                int wordTopicIndex = Integer.parseInt(strArr[strArr.length - 1]);
                Ntd[wordTopicIndex][curDocIndex]++;
            }

            in.close();
            return Ntd;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Integer[] sortTopicIdx(final int[] docScores)
    {
        final Integer[] idx = new Integer[numDocs];
        for(int i=0; i<numDocs; i++){
            idx[i] = i;
        }

        Arrays.sort(idx, new Comparator<Integer>() {
            @Override public int compare(final Integer o1, final Integer o2) {
                return docScores[o1] - docScores[o2];
            }
        });

        return idx;
    }

    public void buildCsv4(String stateFile, int numDocsShown, String outputCsv)        //docs in topic
    {
        Ntd =  buildNtd(numTopics, numDocs, stateFile);
        if (Ntd != null) {
            try {
                FileWriter fwrite = new FileWriter(outputCsv);
                BufferedWriter out = new BufferedWriter(fwrite);
                String header = "topicId" + CSV_DEL + "rank" + CSV_DEL + "docId" + CSV_DEL + "filename";
                out.write(header + "\n");
                String line;
                for (int i = 0; i<numTopics; i++){
                    Integer[] idx = sortTopicIdx(Ntd[i]);
                    for (int j = 0; j<numDocsShown; j++) {                                    //FIXME doc id and number are the same
                        int k = idx[numDocs - j - 1];                                        //Descending
                        line = i + CSV_DEL + j + CSV_DEL + k + CSV_DEL + docNames.get(k) + "\n";
                        out.write(line);
                    }
                }
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("NTB is NULL!!!");
        }
    }


    public String extractFileSubstring(String[] strArr, int startIndex) throws URISyntaxException {
        String filename=strArr[startIndex];
        for (int i = startIndex + 1; i < strArr.length; i++) {
            if (new File(new URI(filename)).exists()) {
                filename = filename + CSV_DEL + i;                // FIXME change to object if possible
                break;
            }
            filename = filename + CSV_DEL + strArr[i];
        }
        return filename;
    }

    public String dtLine2Csv(String line)
    {
        try {
            int start;
            String[] str = line.split("\\t"); // tab as split
            if (str.length >= 2) {
                String csvLine = str[0];
                if (str[1].equals("null-source")) {
                    csvLine  = csvLine + CSV_DEL + str[1];
                    start = 2;
                    docNames.add("null-source");
                } else {
                    String augfile = extractFileSubstring(str, 1);
                    String[] filewnum = augfile.split(CSV_DEL); // has been ', '
                    docNames.add(filewnum[0]);
                    csvLine  = csvLine + CSV_DEL + filewnum[0];
                    start = Integer.parseInt(filewnum[1]);
                }
                for (int i = start; i < str.length - 1; i = i + 2)  {
                    csvLine = csvLine + CSV_DEL + str[i] + CSV_DEL + str[i + 1];
                }
                return csvLine;
            } else {
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String dtLine2dtVec(String line)
    {
        try {
            int start, topic;
            float topicprop;

            ArrayList topics = new ArrayList<String>();
            StringBuilder csvLine = new StringBuilder();

            String[] str = line.split("\\t");  // tab as split
            if (str.length>=2) {
                csvLine.append(str[0]);
                if (str[1].equals("null-source")){
                    csvLine.append(CSV_DEL);
                    csvLine.append(str[1]);
                    start = 2;
                    docNames.add("null-source");
                } else {
                    String augfile = extractFileSubstring(str, 1);
                    String[] filewnum = augfile.split(CSV_DEL);  // has been ', '
                    docNames.add(filewnum[0]);
                    csvLine.append(CSV_DEL);
                    csvLine.append(filewnum[0]);
                    start = Integer.parseInt(filewnum[1]);
                }
                for (int i = start; i < str.length - 1; i = i + 2) {
                    topic = Integer.parseInt(str[i]);
                    while (topic >= topics.size()) {
                        topics.add(0.0);
                    }
                    topics.set(topic, str[i + 1]);
                }
                for (int i = 0; i < topics.size(); i = i + 1)  {
                    csvLine.append(CSV_DEL);
                    csvLine.append(topics.get(i));
                }
                return csvLine.toString();
            } else {
                return line;
            }
         } catch (Exception e) {
            e.printStackTrace();
            return    null;
        }
    }


    public void buildCsv2(String docTopicsFile, String outputCsv)  //topics in doc, as sorted pairs
    {
        buildCsvTopicDoc(docTopicsFile, outputCsv, false);
    }

    public void buildCsv3(String docTopicsFile, String outputCsv)  //topics in doc, as vectors
    {
        buildCsvTopicDoc(docTopicsFile, outputCsv, true);
    }

    public void buildCsvTopicDoc(String docTopicsFile, String outputCsv, Boolean makevec)
    {
        try
        {
            FileReader fread = new FileReader(docTopicsFile);
            BufferedReader in = new BufferedReader(fread);
            String line = null;
            int nd = 0;
            docNames = new ArrayList<String>();

            line = in.readLine();      //skip mallet header line
            if (line!= null)
            {
                FileWriter fwrite = new FileWriter(outputCsv);
                BufferedWriter out = new BufferedWriter(fwrite);
                String header = "docId" + CSV_DEL + "filename" + CSV_DEL + "toptopics...";            //variable number of topics for each doc
                out.write(header + "\n");
                while ((line = in.readLine()) != null)
                {
                    nd++;
                    String csvLine;
                    if (makevec) {
                        csvLine = dtLine2dtVec(line);
                    } else {
                        csvLine = dtLine2Csv(line);
                    }
                    out.write(csvLine + "\n");
                }
                out.flush();
                setNumDocs(nd);
            }

            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setNumDocs(int value)
    {
        numDocs = value;
    }

    public void setNumTopics(int value)
    {
        numTopics = value;
    }

    public void createCsvFiles(int numTopics, String outputDir)
    {
        File csvDir = new File(outputDir + File.separator+ "output_csv");    //FIXME replace all strings with constants
        csvDir.mkdir();
        setNumTopics(numTopics);
        String csvDirPath = csvDir.getPath();
        buildCsv1(outputDir + File.separator + "output_topic_keys", csvDirPath + File.separator + TOPIC_WORDS);
        buildCsv2(outputDir + File.separator + "output_doc_topics.txt", csvDirPath + File.separator + TOPICS_IN_DOCS);
        buildCsv3(outputDir + File.separator + "output_doc_topics.txt", csvDirPath + File.separator + TOPICS_IN_DOCS_VECTORS);
        buildCsv4(outputDir + File.separator + "output_state", Math.min(500, numDocs), csvDirPath + File.separator + DOCS_IN_TOPICS);
    }

    public int[][] getNtd()
    {
        return Ntd;
    }

    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        CsvBuilder o = new CsvBuilder();
    }
}
