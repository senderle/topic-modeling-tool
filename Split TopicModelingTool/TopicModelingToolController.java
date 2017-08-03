package cc.mallet.topics.gui;

import cc.mallet.topics.gui.util.BatchSegmenter;
import cc.mallet.topics.gui.util.CsvWriter;
import cc.mallet.topics.gui.util.FakeMetadata;

import java.awt.*;
import java.awt.event.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.nio.file.Path;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.io.PrintStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;

/**
 * The Class TopicModelingGUI.
 */
public class TopicModelingToolGUI {
    /** delimiter constants */
    public static final String CSV_DEL = ",";
    public static final String MALLET_CSV_DEL = "\\t";
    public static final String NEWLINE = "\n";

    /** filename constants */
    public static final String TOPIC_WORDS = "topic-words.csv";
    public static final String DOCS_IN_TOPICS = "docs-in-topics.csv";
    public static final String TOPICS_IN_DOCS_VECTORS = "topics-metadata.csv";
    public static final String TOPICS_IN_DOCS = "topics-in-docs.csv";

    public static final String MALLET_TOPIC_INPUT = "topic-input.mallet";
    public static final String MALLET_TOPIC_KEYS = "topic-keys.txt";
    public static final String MALLET_STATE = "output-state";
    public static final String MALLET_STATE_GZ = "output-state.gz";
    public static final String MALLET_DOC_TOPICS = "doc-topics.txt";
    public static final String MALLET_WORDS_TOPICS_COUNTS = "words-topics-counts.txt";

    public static final String MALLET_OUT = "output_mallet";
    public static final String CSV_OUT = "output_csv";
    public static final String HTML_OUT = "output_html";

    /** used for testing to set an input dir on startup */
    public static String DEFAULT_INPUT_DIR = "";
    public static String DEFAULT_OUTPUT_DIR = "";
    public static String DEFAULT_METADATA_FILE = "";
    public static String DEFAULT_STOPLIST_FILE = "";

    /** no idea */
    private static final long serialVersionUID = 1L;

    Date timestamp = new Date();

    // Currently always false, but the necessary functionality is 
    // fully built-in! 
    Boolean useTimeStamp = false;

    JFrame rootframe, advancedFrame;
    JPanel mainPanel, advPanel;

    JDialog helpPane1, helpPane2;
    JTextArea log;

    JButton inputDataButton, outputDirButton, trainButton, clearButton,
            advancedButton;
    JCheckBox stopBox;

    JTextField numTopics = new JTextField(2);

    JTextField inputDirTfield = new JTextField();
    JTextField outputDirTfield = new JTextField();
    JTextField stopFileField = new JTextField();
    JTextField metadataFileField = new JTextField();

    ArrayList<JFileChooser> allFileChoosers = new ArrayList<JFileChooser>();

    String inputDirAlternate = null;
    String metadataFileAlternate = null;

    LinkedHashMap<String, String[]> checkBoxOptionMap = new LinkedHashMap<String, String[]>();
    LinkedHashMap<String, String[]> fieldOptionMap = new LinkedHashMap<String, String[]>();

    LinkedHashMap<String, JCheckBox> advCheckBoxMap = new LinkedHashMap<String, JCheckBox>();
    LinkedHashMap<String, JTextField> advFieldMap = new LinkedHashMap<String, JTextField>();

    Boolean frameBusy = false;
    Boolean failOnExc = false;


    // THIS IS A GOD OBJECT. It needs to be refactored into at least three
    // separate classes. But since I don't have time to do that, I'm
    // going through and creating section headers, etc., to show the
    // internal structure more clearly.

    // ////////////////////////////////////////////////// //
    // SECTION ONE: Small Utility Functions and Accessors //
    // ////////////////////////////////////////////////// //

    public TopicModelingTool(boolean isTest) {
        failOnExc = isTest;
    }

    public TopicModelingTool() {
        this(false);
    }

        // ////////////////////////// //
    // SECTION THREE: Actual Work //
    // ////////////////////////// //


    // 1) Create segment directory inside output dir
    // 2) Segment files
    // 3) Save new metadata file to output dir

    public void segmentInput(String delim, int nsegments)
            throws IOException {
        Path inputDirPath = Paths.get(getInputDirName());
        Path outputDirPath = Paths.get(getTimestampedOutputDir());
        Path segmentPath = Paths.get(getTimestampedOutputDir(), "segments");
        Path newMetadataPath = Paths.get(getTimestampedOutputDir(), "segments-metadata.csv");
        Path metadataPath = null;

        if (getMetadataFileName().equals("")) {
            metadataPath = Paths.get(getTimestampedOutputDir(), "autogen-metadata.csv");
            setMetadataFileAlternate(metadataPath.toString());
            FakeMetadata.write(inputDirPath, metadataPath, delim);
        } else {
            metadataPath = Paths.get(getMetadataFileName());
        }

        Files.createDirectories(segmentPath);
        BatchSegmenter bs = new BatchSegmenter(inputDirPath,
                segmentPath, metadataPath, delim);

        ArrayList<String[]> metadataRows = bs.segment(nsegments);

        try (CsvWriter csv = new CsvWriter(newMetadataPath, delim)) {
            csv.writeRows(metadataRows);
        }

        // Modify input and metadata config to point to the correct output:

        setInputDirAlternate(segmentPath.toString());
        setMetadataFileAlternate(newMetadataPath.toString());
    }

    /**
     * Method that assembles all the options given by the user through the GUI
     * and runs Mallet's importing and topic modeling methods.
     */
    public void runMallet() {

        // ////////////// //
        // Initialize GUI //
        // ////////////// //

        // Keep track of time elapsed
        timestamp = new Date();
        long start = System.currentTimeMillis();

        // Disable user input during training
        clearButton.setEnabled(false);
        trainButton.setEnabled(false);

        int nsegments =
            Integer.parseInt(advFieldMap.get("io-segment-files").getText());
        String delim = null;

        if (nsegments > 0) {
            delim = advFieldMap.get("io-metadata-delimiter").getText();
            delim = escapeTab(delim);

            appendLog("Automatically segmenting files...");

            try {
                segmentInput(delim, nsegments);
            } catch (IOException exc) {
                errorLog(exc);
                runMalletCleanup();
                return;
            }
        }

        // //////////////////// //
        // Build Argument Lists //
        // //////////////////// //

        // INPUT: output of getAdvArgs()
        // OUTPUT: arglists, optionMaps

        HashMap<String, ArrayList<String>> arglists =
            new HashMap<String, ArrayList<String>>();
        ArrayList<LinkedHashMap<String, String[]>> optionMaps =
            new ArrayList<LinkedHashMap<String, String[]>>();

        arglists.put("import", new ArrayList<String>());
        arglists.put("train", new ArrayList<String>());
        optionMaps.add(fieldOptionMap);
        optionMaps.add(checkBoxOptionMap);

        // ////// //
        // Import //
        // ////// //

        // INPUT: input dir field (via GUI), output dir field (via GUI),
        //        arglists; if output factored out, need import filename
        //        (i.e. var with value of "topic-input.mallet" below)
        // OUTPUT: collectionPath, inputDir, outputDir -- all can be factored
        //         out!

        String inputDir = getInputDirName();
        String outputDir = getTimestampedOutputDir();

        String malletPath = Paths.get(outputDir, MALLET_OUT).toString();
        String csvPath = Paths.get(outputDir, CSV_OUT).toString();
        String htmlPath = Paths.get(outputDir, HTML_OUT).toString();
        Paths.get(malletPath).toFile().mkdirs();
        Paths.get(csvPath).toFile().mkdirs();
        Paths.get(htmlPath).toFile().mkdirs();

        String collectionPath = Paths.get(malletPath, MALLET_TOPIC_INPUT).toString();


        appendLog("");
        appendLog("Importing and Training");
        appendLog("This could take minutes or days depending on settings and corpus size.");
        appendLog("");

        String malletImportCmd = "";
        Class<?> importClass = null;
        String[] importArgs = null;
        Class<?>[] importArgTypes = new Class<?>[1];
        Object[] importPassedArgs = new Object[1];

        if (!getStopFileName().equals("")) {
            if (advCheckBoxMap.get("--remove-stopwords").isSelected()) {
                arglists.get("import").add("--extra-stopwords");
            } else {
                arglists.get("import").add("--stoplist-file");

                // For some reason, once you disable this, it becomes
                // impossible to reenable. You can check the box, but MALLET
                // ignores the argument! I don't understand why. So once it's
                // disabled once, it's completely grayed out. That will
                // produce confusion, but that's better than producing 
                // subtle, easy-to-miss, incorrect behavior.
                advCheckBoxMap.get("--remove-stopwords").setEnabled(false);
            }

            arglists.get("import").add(getStopFileName());
        }

        arglists.get("import").addAll(getAdvArgs("import"));
        arglists.get("import").addAll(Arrays.asList(
                "--input", inputDir,
                "--output", collectionPath,
                "--keep-sequence")
        );

        importArgs = arglists.get("import").toArray(
                new String[arglists.get("import").size()]
        );
        importArgTypes[0] = importArgs.getClass();
        importPassedArgs[0] = importArgs;

        try {
            // This is all hard-coded because we assume MALLET's API and
            // command structure will be stable.
            if ((new File(inputDir)).isDirectory()) {
                importClass = Class.forName("cc.mallet.classify.tui.Text2Vectors");
                malletImportCmd = "import-dir";
            } else {
                importClass = Class.forName("cc.mallet.classify.tui.Csv2Vectors");
                malletImportCmd = "import-file";
            }
        } catch (ClassNotFoundException exc) {
            errorLog(exc);
            runMalletCleanup();
            return;
        }

        appendLog("** Importing From " + inputDir + " **");
        appendLog("");
        appendLog("Mallet command: ");
        appendLog("    " + formatMalletCommand(malletImportCmd, importArgs));
        appendLog("");
        appendLog("");
        appendLog("--- Start of Mallet Output ---");
        appendLog("");
        updateStatusCursor("Importing...");

        // The only thing that should actually have a blanket catch statement:
        try {
            importClass.getMethod("main", importArgTypes)
                .invoke(importClass.newInstance(), importPassedArgs);
        } catch (Throwable exc) {
            errorLog(exc);
            runMalletCleanup();
            return;
        }

        // ///// //
        // Train //
        // ///// //

        // INPUT: outputDir, collectionPath (derivable from outputDir + hard-coded thing above), numTopics,
        // OUTPUT, none, effectively, I think?

        outputDir = getTimestampedOutputDir();
        String stateFile = Paths.get(outputDir, MALLET_OUT, MALLET_STATE_GZ).toString();
        String topicKeysFile = Paths.get(outputDir, MALLET_OUT, MALLET_TOPIC_KEYS).toString();
        String outputDocTopicsFile = Paths.get(outputDir, MALLET_OUT, MALLET_DOC_TOPICS).toString();
        String wordsTopicCountsFile = Paths.get(outputDir, MALLET_OUT, MALLET_WORDS_TOPICS_COUNTS).toString();

        Class<?> trainClass = null;
        String[] trainArgs = null;
        Class<?>[] trainArgTypes = new Class<?>[1];
        Object[] trainPassedArgs = new Object[1];

        arglists.get("train").addAll(getAdvArgs("train"));
        arglists.get("train").addAll(Arrays.asList(
                "--input", collectionPath,
                "--num-topics", numTopics.getText(),
                "--output-state", stateFile,
                "--output-topic-keys", topicKeysFile,
                "--output-doc-topics", outputDocTopicsFile,
                "--word-topic-counts-file", wordsTopicCountsFile)
        );

        trainArgs = arglists.get("train").toArray(
                new String[arglists.get("train").size()]
        );

        trainArgTypes[0] = trainArgs.getClass();
        trainPassedArgs[0] = trainArgs;

        try {
            trainClass = Class.forName("cc.mallet.topics.tui.Vectors2Topics");
        } catch (ClassNotFoundException exc) {
            errorLog(exc);
            runMalletCleanup();
            return;
        }

        appendLog("");
        appendLog("--- End of Mallet Output ---");
        appendLog("");
        appendLog("");
        appendLog("Import successful.");
        appendLog("");
        appendLog("** Training **");
        appendLog("");
        appendLog("Mallet command: ");
        appendLog("    " + formatMalletCommand("train-topics", trainArgs));
        appendLog("");
        appendLog("");
        appendLog("--- Start of Mallet Output ---");
        appendLog("");
        updateStatusCursor("Training...");

        // The only thing that should actually have a blanket catch statement:
        try {
            trainClass.getMethod("main", trainArgTypes)
                .invoke(trainClass.newInstance(), trainPassedArgs);
        } catch (Throwable exc) {
            errorLog(exc);
            runMalletCleanup();
            return;
        }

        // /////////////// //
        // Generate Output //
        // /////////////// //

        appendLog("");
        appendLog("--- End of Mallet Output ---");
        appendLog("");
        appendLog("");
        appendLog("Training successful.");
        appendLog("");
        appendLog("** Generating Output **");
        updateStatusCursor("Generating output...");

        try {
            outputCsvFiles(outputDir,
                    advCheckBoxMap.get("io-generate-html").isSelected(),
                    advCheckBoxMap.get("io-preserve-mallet").isSelected());
        } catch (Throwable exc) {
            errorLog(exc);
            runMalletCleanup();
            return;
        }

        // //////////////////////////// //
        // Report Results and Reset GUI //
        // //////////////////////////// //

        appendLog("");
        if (advCheckBoxMap.get("io-preserve-mallet").isSelected()) {
            appendLog("Mallet Output files written in " 
                    + Paths.get(outputDir, MALLET_OUT).toString());
        }
        if (advCheckBoxMap.get("io-generate-html").isSelected()) {
            appendLog("Html Output files written in " 
                    + Paths.get(outputDir, HTML_OUT).toString());
        }

        appendLog("Csv Output files written in " 
                + Paths.get(outputDir, CSV_OUT).toString());

        log.setCaretPosition(log.getDocument().getLength());
        clearButton.setEnabled(true);

        long elapsedTimeMillis = System.currentTimeMillis() - start;

        // Get elapsed time in seconds
        float elapsedTimeSec = elapsedTimeMillis/1000F;
        appendLog("Time (including output generation): " + elapsedTimeSec);
        appendLog("");

        runMalletCleanup();
    }

    public void runMalletCleanup() {
        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        // Reenable the "Learn Topics" button -- this should happen even
        // on unexpected exits.
        //
        // Eventually, a global `try... finally` should run this,
        // once the runMallet routines are broken out into individual methods.
        trainButton.setText("Learn Topics");
        trainButton.setEnabled(true);

        // Idempotently reset any temporary assignments to the input and
        // metadata fields. This allows us to temporarily override
        // those values if necessary. If we have overriden them, these
        // reset the values; otherwise, these operations have no effect.
        setInputDirAlternate();
        setMetadataFileAlternate();

        rootframe.setCursor(normalCursor);
        frameBusy = false;
    }

    /**
    * Output csv files.
    *
    * @param outputDir the output directory
    * @param htmlOutputFlag print html output or not
    */
    private void outputCsvFiles(String outputDir,
            Boolean htmlOutputFlag,
            Boolean preserveMalletFilesFlag)
        throws IOException {
        CsvBuilder makecsv = new CsvBuilder(
            Integer.parseInt(numTopics.getText()),
            escapeTab(advFieldMap.get("io-metadata-delimiter").getText()),
            escapeTab(advFieldMap.get("io-output-delimiter").getText())
        );
        makecsv.createCsvFiles(outputDir, getMetadataFileName());

        if (htmlOutputFlag) {
            HtmlBuilder hb = new HtmlBuilder(
                    makecsv.getNtd(),
                    new File(getInputDirName()),
                    advFieldMap.get("io-output-delimiter").getText()
            );
            hb.createHtmlFiles(new File(outputDir));
        }

        if (!preserveMalletFilesFlag) {
            clearExtrafiles(outputDir);
        }
    }

    private void clearExtrafiles(String outputDir) {
        String[] fileNames = new String[5];
        fileNames[0] = Paths.get(MALLET_OUT, MALLET_TOPIC_INPUT).toString();
        fileNames[1] = Paths.get(MALLET_OUT, MALLET_TOPIC_KEYS).toString();
        fileNames[2] = Paths.get(MALLET_OUT, MALLET_STATE_GZ).toString();
        fileNames[3] = Paths.get(MALLET_OUT, MALLET_DOC_TOPICS).toString();
        fileNames[4] = Paths.get(MALLET_OUT, MALLET_WORDS_TOPICS_COUNTS).toString();
        //fileNames[5] = Paths.get(MALLET_OUT, MALLET_STATE).toString();

        for (String f:fileNames) {
            if (!(new File(outputDir, f).canWrite())) {
                appendLog("clearExtrafiles failed on ");
                appendLog(f);
            }
            Paths.get(outputDir, f).toFile().delete();
        }

        Paths.get(outputDir, MALLET_OUT).toFile().delete();
    }
}