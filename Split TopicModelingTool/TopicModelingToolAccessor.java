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

public class TopicModelingToolAccessor {
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

    public String getInputDirName() {
        if (inputDirAlternate == null) {
            return inputDirTfield.getText();
        } else {
            return inputDirAlternate;
        }
    }

    private void setInputDirAlternate(String in) {
        inputDirAlternate = in;
    }

    private void setInputDirAlternate() {
        inputDirAlternate = null;
    }

    public String getMetadataFileName() {
        if (metadataFileAlternate == null) {
            return metadataFileField.getText();
        } else {
            return metadataFileAlternate;
        }
    }

    private void setMetadataFileAlternate(String meta) {
        metadataFileAlternate = meta;
    }

    private void setMetadataFileAlternate() {
        metadataFileAlternate = null;
    }

    public String getOutputDir() {
        return outputDirTfield.getText();
    }

    public static Path getUserHomePath() {
        String home = System.getProperty("user.home");
        Path desktop = Paths.get(home, "Desktop");
        if (Files.isDirectory(desktop)) {
            return desktop;
        } else {
            return Paths.get(home);
        }
    }

    public String getTimestampedOutputDir() {
        Path dir = null;
        if (useTimeStamp) {
    	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH.mm") ;
    	    String name = dateFormat.format(timestamp);
            dir = Paths.get(getOutputDir(), "output-" + name);
        } else {
            dir = Paths.get(getOutputDir());
        }
        dir.toFile().mkdirs();
    	return dir.toString();
    }
    
    public String getStopFileName() {
        return stopFileField.getText();
    }

    /**
     * Update text area.
     *
     * @param text the text
     */
    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                log.append(text);
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void appendLog(String... lines) {
        for (String ln : lines) {
            log.append(ln);
            log.append(NEWLINE);
        }
        log.setCaretPosition(log.getDocument().getLength());
    }

    private void errorLog(Throwable exc) {
        if (failOnExc) {
            throw new RuntimeException(exc);
        }

        appendLog("");
        appendLog(" ************************************");
        appendLog(" **** **   Unexpected Error   ** ****");
        appendLog(" **** -- Start System Message -- ****");
        appendLog("");

        StringWriter traceStringWriter = new StringWriter();
        exc.printStackTrace(new PrintWriter(traceStringWriter));
        appendLog(traceStringWriter.toString());
        appendLog("");
        appendLog(" **** --  End System Message  -- ****");
        appendLog(" ************************************");
        appendLog("");
        appendLog("Resetting Tool...");
    }

    private static String escapeTab(String in) {
        return in.replace("\\t", "\t");
    }

    private String formatMalletCommand(String cmd, String[] args) {
        StringBuilder cmdstr = new StringBuilder();
        cmdstr.append("mallet ");
        cmdstr.append(cmd);
        cmdstr.append(" ");
        for (String arg : args) {
            cmdstr.append(arg);
            cmdstr.append(" ");
        }

        return cmdstr.toString().replaceAll("--", " \\\\" + NEWLINE + "\t--");
    }

    /**
     * Redirect system streams.
     */
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}