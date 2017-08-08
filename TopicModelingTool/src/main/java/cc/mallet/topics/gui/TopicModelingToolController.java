package cc.mallet.topics.gui;

import cc.mallet.topics.gui.util.BatchSegmenter;
import cc.mallet.topics.gui.util.CsvWriter;
import cc.mallet.topics.gui.util.FakeMetadata;

import java.awt.*;
import java.awt.event.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.*;

import java.util.*;
import javax.swing.*;

import cc.mallet.topics.gui.TopicModelingToolAccessor;

public class TopicModelingToolController {
    /** filename constants */
    public static final String MALLET_TOPIC_INPUT = "topic-input.mallet";
    public static final String MALLET_TOPIC_KEYS = "topic-keys.txt";
    public static final String MALLET_STATE = "output-state";
    public static final String MALLET_STATE_GZ = "output-state.gz";
    public static final String MALLET_DOC_TOPICS = "doc-topics.txt";
    public static final String MALLET_WORDS_TOPICS_COUNTS = "words-topics-counts.txt";

    public static final String MALLET_OUT = "output_mallet";
    public static final String CSV_OUT = "output_csv";
    public static final String HTML_OUT = "output_html";

    public static final String CSV_DEL = ",";
    public static final String MALLET_CSV_DEL = "\\t";

    public static final String TOPIC_WORDS = "topic-words.csv";
    public static final String DOCS_IN_TOPICS = "docs-in-topics.csv";
    public static final String TOPICS_IN_DOCS_VECTORS = "topics-metadata.csv";
    public static final String TOPICS_IN_DOCS = "topics-in-docs.csv";

    private TopicModelingToolAccessor accessor;
    private TopicModelingToolGUI gui;

    public TopicModelingToolController(TopicModelingToolAccessor accessor, 
        TopicModelingToolGUI gui) {
        this.accessor = accessor;
        this.gui = gui;
    }

    public TopicModelingToolController() {
        this.accessor = new TopicModelingToolAccessor();
    }

    // ////////////////////////// //
    // SECTION THREE: Actual Work //
    // ////////////////////////// //


    // 1) Create segment directory inside output dir
    // 2) Segment files
    // 3) Save new metadata file to output dir


    public void segmentInput(String delim, int nsegments)
            throws IOException {
        Path inputDirPath = Paths.get(this.accessor.getInputDirName());
        Path outputDirPath = Paths.get(this.accessor.getTimestampedOutputDir());
        Path segmentPath = Paths.get(this.accessor.getTimestampedOutputDir(), "segments");
        Path newMetadataPath = Paths.get(this.accessor.getTimestampedOutputDir(), 
            "segments-metadata.csv");
        Path metadataPath = null;

        if (this.accessor.getMetadataFileName().equals("")) {
            metadataPath = Paths.get(this.accessor.getTimestampedOutputDir(), 
                "autogen-metadata.csv");
            this.accessor.setMetadataFileAlternate(metadataPath.toString());
            FakeMetadata.write(inputDirPath, metadataPath, delim);
        } else {
            metadataPath = Paths.get(this.accessor.getMetadataFileName());
        }

        Files.createDirectories(segmentPath);
        BatchSegmenter bs = new BatchSegmenter(inputDirPath,
                segmentPath, metadataPath, delim);

        ArrayList<String[]> metadataRows = bs.segment(nsegments);

        try (CsvWriter csv = new CsvWriter(newMetadataPath, delim)) {
            csv.writeRows(metadataRows);
        }

        // Modify input and metadata config to point to the correct output:

        this.accessor.setInputDirAlternate(segmentPath.toString());
        this.accessor.setMetadataFileAlternate(newMetadataPath.toString());
    }

    /**
     * Method that assembles all the options given by the user through the GUI
     * and runs Mallet's importing and topic modeling methods.
     */
    public void runMallet(LinkedHashMap<String, OptionStrings> checkBoxOptionMap, 
                          LinkedHashMap<String, OptionStrings> fieldOptionMap,
                          LinkedHashMap<String, JTextField> advFieldMap,
                          LinkedHashMap<String, JCheckBox> advCheckBoxMap) {

        // ////////////// //
        // Initialize GUI //
        // ////////////// //

        // Keep track of time elapsed
        this.accessor.generateDate();
        long start = System.currentTimeMillis();

        // Disable user input during training
        this.gui.enableClearButton(false);
        this.gui.enableTrainButton(false);

        int nsegments =
            Integer.parseInt(advFieldMap.get("io-segment-files").getText());
        String delim = null;

        if (nsegments > 0) {
            delim = advFieldMap.get("io-metadata-delimiter").getText();
            delim = TopicModelingToolAccessor.escapeTab(delim);

            this.accessor.appendLog("Automatically segmenting files...");

            try {
                segmentInput(delim, nsegments);
            } catch (IOException exc) {
                this.accessor.errorLog(exc);
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
        ArrayList<LinkedHashMap<String, OptionStrings>> optionMaps =
            new ArrayList<LinkedHashMap<String, OptionStrings>>();

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

        String inputDir = this.accessor.getInputDirName();
        String outputDir = this.accessor.getTimestampedOutputDir();

        String malletPath = Paths.get(outputDir, MALLET_OUT).toString();
        String csvPath = Paths.get(outputDir, CSV_OUT).toString();
        String htmlPath = Paths.get(outputDir, HTML_OUT).toString();
        Paths.get(malletPath).toFile().mkdirs();
        Paths.get(csvPath).toFile().mkdirs();
        Paths.get(htmlPath).toFile().mkdirs();

        String collectionPath = Paths.get(malletPath, MALLET_TOPIC_INPUT).toString();


        this.accessor.appendLog("");
        this.accessor.appendLog("Importing and Training");
        this.accessor.appendLog(
            "This could take minutes or days depending on settings and corpus size.");
        this.accessor.appendLog("");

        String malletImportCmd = "";
        Class<?> importClass = null;
        String[] importArgs = null;
        Class<?>[] importArgTypes = new Class<?>[1];
        Object[] importPassedArgs = new Object[1];

        if (!(this.accessor.getStopFileName().equals(""))) {
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

            arglists.get("import").add(this.accessor.getStopFileName());
        }

        arglists.get("import").addAll(this.gui.getAdvArgs("import"));
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
            this.accessor.errorLog(exc);
            runMalletCleanup();
            return;
        }

        this.accessor.appendLog("** Importing From " + inputDir + " **");
        this.accessor.appendLog("");
        this.accessor.appendLog("Mallet command: ");
        this.accessor.appendLog("    " + this.accessor.formatMalletCommand(malletImportCmd, importArgs));
        this.accessor.appendLog("");
        this.accessor.appendLog("");
        this.accessor.appendLog("--- Start of Mallet Output ---");
        this.accessor.appendLog("");
        this.gui.updateStatusCursor("Importing...");

        // The only thing that should actually have a blanket catch statement:
        try {
            importClass.getMethod("main", importArgTypes)
                .invoke(importClass.newInstance(), importPassedArgs);
        } catch (Throwable exc) {
            this.accessor.errorLog(exc);
            runMalletCleanup();
            return;
        }

        // ///// //
        // Train //
        // ///// //

        // INPUT: outputDir, collectionPath (derivable from outputDir + hard-coded thing above), 
        // numTopics,
        // OUTPUT, none, effectively, I think?

        outputDir = this.accessor.getTimestampedOutputDir();
        String stateFile = Paths.get(outputDir, MALLET_OUT, MALLET_STATE_GZ).toString();
        String topicKeysFile = Paths.get(outputDir, MALLET_OUT, MALLET_TOPIC_KEYS).toString();
        String outputDocTopicsFile = Paths.get(outputDir, MALLET_OUT, MALLET_DOC_TOPICS).toString();
        String wordsTopicCountsFile = Paths.get(outputDir, MALLET_OUT, 
            MALLET_WORDS_TOPICS_COUNTS).toString();

        Class<?> trainClass = null;
        String[] trainArgs = null;
        Class<?>[] trainArgTypes = new Class<?>[1];
        Object[] trainPassedArgs = new Object[1];

        arglists.get("train").addAll(this.gui.getAdvArgs("train"));
        arglists.get("train").addAll(Arrays.asList(
                "--input", collectionPath,
                "--num-topics", this.gui.getNumTopics().getText(),
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
            this.accessor.errorLog(exc);
            runMalletCleanup();
            return;
        }

        this.accessor.appendLog("");
        this.accessor.appendLog("--- End of Mallet Output ---");
        this.accessor.appendLog("");
        this.accessor.appendLog("");
        this.accessor.appendLog("Import successful.");
        this.accessor.appendLog("");
        this.accessor.appendLog("** Training **");
        this.accessor.appendLog("");
        this.accessor.appendLog("Mallet command: ");
        this.accessor.appendLog("    " + this.accessor.formatMalletCommand("train-topics", trainArgs));
        this.accessor.appendLog("");
        this.accessor.appendLog("");
        this.accessor.appendLog("--- Start of Mallet Output ---");
        this.accessor.appendLog("");
        this.gui.updateStatusCursor("Training...");

        // The only thing that should actually have a blanket catch statement:
        try {
            trainClass.getMethod("main", trainArgTypes)
                .invoke(trainClass.newInstance(), trainPassedArgs);
        } catch (Throwable exc) {
            this.accessor.errorLog(exc);
            runMalletCleanup();
            return;
        }

        // /////////////// //
        // Generate Output //
        // /////////////// //

        this.accessor.appendLog("");
        this.accessor.appendLog("--- End of Mallet Output ---");
        this.accessor.appendLog("");
        this.accessor.appendLog("");
        this.accessor.appendLog("Training successful.");
        this.accessor.appendLog("");
        this.accessor.appendLog("** Generating Output **");
        this.gui.updateStatusCursor("Generating output...");

        try {
            outputCsvFiles(outputDir,
                    advCheckBoxMap.get("io-generate-html").isSelected(),
                    advCheckBoxMap.get("io-preserve-mallet").isSelected(),
                    advFieldMap);
        } catch (Throwable exc) {
            this.accessor.errorLog(exc);
            runMalletCleanup();
            return;
        }

        // //////////////////////////// //
        // Report Results and Reset GUI //
        // //////////////////////////// //

        this.accessor.appendLog("");
        if (advCheckBoxMap.get("io-preserve-mallet").isSelected()) {
            this.accessor.appendLog("Mallet Output files written in " 
                    + Paths.get(outputDir, MALLET_OUT).toString());
        }
        if (advCheckBoxMap.get("io-generate-html").isSelected()) {
            this.accessor.appendLog("Html Output files written in " 
                    + Paths.get(outputDir, HTML_OUT).toString());
        }

        this.accessor.appendLog("Csv Output files written in " 
                + Paths.get(outputDir, CSV_OUT).toString());

        this.accessor.setLogCaretPosition();
        this.gui.enableClearButton(true);

        long elapsedTimeMillis = System.currentTimeMillis() - start;

        // Get elapsed time in seconds
        float elapsedTimeSec = elapsedTimeMillis/1000F;
        this.accessor.appendLog("Time (including output generation): " + elapsedTimeSec);
        this.accessor.appendLog("");

        runMalletCleanup();
    }

    public void runMalletCleanup() {
        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        // Reenable the "Learn Topics" button -- this should happen even
        // on unexpected exits.
        //
        // Eventually, a global `try... finally` should run this,
        // once the runMallet routines are broken out into individual methods.
        this.gui.setTrainButton("Learn Topics");
        this.gui.enableTrainButton(true);

        // Idempotently reset any temporary assignments to the input and
        // metadata fields. This allows us to temporarily override
        // those values if necessary. If we have overriden them, these
        // reset the values; otherwise, these operations have no effect.
        this.accessor.setInputDirAlternate();
        this.accessor.setMetadataFileAlternate();

        this.gui.setRootFrame(normalCursor);
        this.gui.setFrameBusy(false);
    }

    /**
    * Output csv files.
    *
    * @param outputDir the output directory
    * @param htmlOutputFlag print html output or not
    */
    public void outputCsvFiles(String outputDir,
            Boolean htmlOutputFlag,
            Boolean preserveMalletFilesFlag,
            LinkedHashMap<String, JTextField> advFieldMap)
        throws IOException {
        CsvBuilder makecsv = new CsvBuilder(
            Integer.parseInt(this.gui.getNumTopics().getText()),
            TopicModelingToolAccessor.escapeTab(advFieldMap.get("io-metadata-delimiter").getText()),
            TopicModelingToolAccessor.escapeTab(advFieldMap.get("io-output-delimiter").getText())
        );
        makecsv.createCsvFiles(outputDir, this.accessor.getMetadataFileName());

        if (htmlOutputFlag) {
            HtmlBuilder hb = new HtmlBuilder(
                    makecsv.getNtd(),
                    new File(this.accessor.getInputDirName()),
                    advFieldMap.get("io-output-delimiter").getText()
            );
            hb.createHtmlFiles(new File(outputDir));
        }

        if (!preserveMalletFilesFlag) {
            clearExtrafiles(outputDir);
        }
    }

    public void clearExtrafiles(String outputDir) {
        String[] fileNames = new String[5];
        fileNames[0] = Paths.get(MALLET_OUT, MALLET_TOPIC_INPUT).toString();
        fileNames[1] = Paths.get(MALLET_OUT, MALLET_TOPIC_KEYS).toString();
        fileNames[2] = Paths.get(MALLET_OUT, MALLET_STATE_GZ).toString();
        fileNames[3] = Paths.get(MALLET_OUT, MALLET_DOC_TOPICS).toString();
        fileNames[4] = Paths.get(MALLET_OUT, MALLET_WORDS_TOPICS_COUNTS).toString();

        for (String f:fileNames) {
            if (!(new File(outputDir, f).canWrite())) {
                this.accessor.appendLog("clearExtrafiles failed on ");
                this.accessor.appendLog(f);
            }
            Paths.get(outputDir, f).toFile().delete();
        }

        Paths.get(outputDir, MALLET_OUT).toFile().delete();
    }
}