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
public class TopicModelingTool {
    /** delimiter constants */
    public static final String CSV_DEL = ",";
    public static final String MALLET_CSV_DEL = "\\t";
    public static final String NEWLINE = "\n";

    /** filename constants */
    public static final String TOPIC_WORDS = "TopicWords.csv";
    public static final String DOCS_IN_TOPICS = "DocsInTopics.csv";
    public static final String TOPICS_IN_DOCS_VECTORS = "TopicsMetadata.csv";
    public static final String TOPICS_IN_DOCS = "TopicsInDocs.csv";

    public static final String MALLET_TOPIC_INPUT = "topic-input.mallet";
    public static final String MALLET_TOPIC_KEYS = "output_topic_keys.txt";
    public static final String MALLET_STATE = "output_state";
    public static final String MALLET_STATE_GZ = "output_state.gz";
    public static final String MALLET_DOC_TOPICS = "output_doc_topics.txt";
    public static final String MALLET_WORDS_TOPICS_COUNTS = "words_topics_counts.txt";

    /** used for testing to set an input dir on startup */
    public static String DEFAULT_INPUT_DIR = "";
    public static String DEFAULT_OUTPUT_DIR = "";
    public static String DEFAULT_METADATA_FILE = "";
    public static String DEFAULT_STOPLIST_FILE = "";

    /** no idea */
    private static final long serialVersionUID = 1L;


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

    public String getOutputDirName() {
        return outputDirTfield.getText();
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

        appendLog(" **** Unexpected Error **** ");
        appendLog("");
        appendLog(" **** -- Start System Message -- ****");
        appendLog("");

        StringWriter traceStringWriter = new StringWriter();
        exc.printStackTrace(new PrintWriter(traceStringWriter));
        appendLog(traceStringWriter.toString());
        appendLog("");
        appendLog(" **** -- End System Message -- ****");
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

    // //////////////////////////////////// //
    // SECTION TWO: Building GUI Components //
    // //////////////////////////////////// //

    /**
     * Creates the help panel in the Basic window.
     */
    public void createHelp1() {

        helpPane1 = new JDialog();
        JPanel p1 = new JPanel();
        String text = "<html><b>Input - </b>Select a directory containing text files, or a single text file where each line is a"+
    " data instance.<br><br> <b>Output Directory -</b> All generated output is written to this folder. Current directory by default.<br><br>"+
    "<b>Number of Topics -</b> The number of topics to fit.<br></html>";

        JLabel b = new JLabel(text, SwingConstants.LEFT) {
            public Dimension getPreferredSize() {
                return new Dimension(400, 150);
            }
            public Dimension getMinimumSize() {
                return new Dimension(400, 150);
            }
            public Dimension getMaximumSize() {
                return new Dimension(400, 150);
            }
        };
        b.setVerticalAlignment(SwingConstants.CENTER);

        p1.add(b);
        p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Help"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        helpPane1.setContentPane(p1);
        helpPane1.setTitle("Basic Options");
        helpPane1.setResizable(false);
        helpPane1.pack();
        helpPane1.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    /**
     * Show help in Basic panel.
     */
    public void showHelp1() {
        helpPane1.setVisible(true);
    }

    /**
     * Show help in Advanced panel.
     */
    public void showHelp2() {
        helpPane2.setVisible(true);
    }

    /**
     * Creates the help panel in the Advanced window.
     */
    public void createHelp2() {
        helpPane2 = new JDialog();
        JPanel p1 = new JPanel();

        String text = "<html><b>Remove stopwords - </b>If checked, remove a list of \"stop words\" from the text.<br><br>"+
    " <b>Stopword file - </b>Read \"stop words\" from a file, one per line. Default is Mallet's list of standard English stopwords.<br><br>"+
    "<b>Case sensitive - </b>If checked, do not force all strings to lowercase.<br><br><b>No. of iterations - </b> The number of iterations of Gibbs sampling to run. Default is 1000.<br><br>" +
    "<b>No. of topic words printed - </b>The number of most probable words to print for each topic after model estimation.<br><br>"+
    "<b>Topic proportion threshold - </b>Do not print topics with proportions less than this threshold value.</b></html>";

        JLabel b = new JLabel(text, SwingConstants.LEFT) {
            public Dimension getPreferredSize() {
                return new Dimension(400, 300);
            }
            public Dimension getMinimumSize() {
                return new Dimension(400, 300);
            }
            public Dimension getMaximumSize() {
                return new Dimension(400, 300);
            }
        };
        b.setVerticalAlignment(SwingConstants.CENTER);

        p1.add(b);
        p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Help"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        helpPane2.setContentPane(p1);
        helpPane2.setTitle("Advanced Options");
        helpPane2.setResizable(false);
        helpPane2.pack();
        helpPane2.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    /**
     * The listener interface for receiving openButton events. The same interface is used for both the input
     * and output directory options
     *
     */
    public class OpenButtonListener implements ActionListener {
        JFileChooser filechooser;
        JTextField filefield;
        String filedescription;

        public OpenButtonListener(
                JFileChooser filech,
                JTextField filef,
                String filed) {
            filechooser = filech;
            filefield = filef;
            filedescription = filed;
        }

        /* (non-Javadoc)
        * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
        */
        public void actionPerformed(ActionEvent e) {
            int returnVal = filechooser.showOpenDialog(mainPanel);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = filechooser.getSelectedFile();
                String inputDir = "";

                try {
                    inputDir = file.getCanonicalPath();
                } catch (IOException ioe) {
                    inputDir = file.getAbsolutePath();
                }

                String inputType = "";

                if (file.isDirectory()) {
                    inputType = " Directory: ";
                } else {
                    inputType = " File: ";
                }

                appendLog("Chose " + filedescription + inputType + inputDir);

                filefield.setText(inputDir);

                for (JFileChooser chooser : allFileChoosers) {
                    chooser.setCurrentDirectory(file.getParentFile());
                }

            } else {
                appendLog("Open command cancelled by user.");
            }
        }
    }

    /**
     * The listener interface for receiving frameFocus events.
     * To show a busy hour glass icon when the Basic window is in focus.
     */
    public class FrameFocusListener implements FocusListener{

       /* (non-Javadoc)
        * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
        */
       @Override
       public void focusGained(FocusEvent arg0) {
           if(frameBusy){
               Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
               rootframe.setCursor(hourglassCursor);
           }
       }

       /* (non-Javadoc)
        * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
        */
       @Override
       public void focusLost(FocusEvent arg0) {
       }
    }

    /**
     * The listener interface for receiving advancedButton events.
     * Clicking should bring up the Advanced panel.
     *
     */
    public class AdvancedButtonListener implements ActionListener{

       /* (non-Javadoc)
        * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
        */
       public void actionPerformed(ActionEvent e) {
            advancedFrame.setVisible(true);

       }
    }

    /**
     * The listener interface for receiving trainButton events.
     *
     */
    public class TrainButtonListener implements ActionListener {
        /*
         * Start a new thread that will execute the runMallet method
         */

        Thread t;
        public void actionPerformed(ActionEvent e) {
            // Get current time
            t = new Thread() {
                public void run() {
                    if (getInputDirName().equals("")) {
                        JOptionPane.showMessageDialog(mainPanel, "Please select an input file or directory", "Invalid input", JOptionPane.ERROR_MESSAGE);
                    } else {
                        runMallet();
                    }
                }
            };
            t.start();
        }

    }

    public void updateStatusCursor(String statusMessage) {
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        rootframe.setCursor(hourglassCursor);
        frameBusy = true;
        trainButton.setText(statusMessage);
    }

    /**
     * Clear console area
     */
    public class ClearButtonListener implements ActionListener {

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            log.setText("");
        }
    }

    /**
    * The listener interface for receiving resetButton events.
    */
    public class ResetButtonListener implements ActionListener {
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            resetAdvControls();
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path the path
     * @return the image icon
     */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = TopicModelingTool.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     *
     * Set corresponding special string, default value, description and associated command for the options
     */
    public void setDefaultOptions() {
        // Field Format:
        // widgetMap.put("--MALLET-OPTION-or-io-key", new String[]
        //      {"Widget Label", "Default Value", "Widget Category", "Autogenerate Widget?"});

        //// Nonstandard options ////

        // (These are manually generated and appear at the top of the
        // advanced window).
        fieldOptionMap.put("io-metadata", new String[]
                {"Metadata File", DEFAULT_METADATA_FILE, "io", "FALSE"});
        fieldOptionMap.put("--stoplist-file", new String[]
                {"Custom Stoplist File", DEFAULT_STOPLIST_FILE, "import", "FALSE"});

        //// Checkboxes ////

        checkBoxOptionMap.put("--remove-stopwords", new String[]
                {"Remove default English stopwords", "TRUE", "import", "TRUE"});
        checkBoxOptionMap.put("--preserve-case", new String[]
                {"Preserve case ", "FALSE", "import", "TRUE"});
        checkBoxOptionMap.put("io-generate-html", new String[]
                {"Generate HTML output", "TRUE", "io", "TRUE"});
        checkBoxOptionMap.put("io-preserve-mallet", new String[]
                {"Preserve raw MALLET output", "FALSE", "io", "TRUE"});

        //// Importing field options ////

        // This regex accepts all unicode characters.
        fieldOptionMap.put("--token-regex", new String[]
                {"Tokenize with regular expression", "[\\p{L}\\p{N}_]+", "import", "TRUE"});

        //// Training field options ////

        fieldOptionMap.put("--num-iterations", new String[]
                {"Number of iterations ", "400", "train", "TRUE"});
        fieldOptionMap.put("--num-top-words", new String[]
                {"Number of topic words to print ", "20", "train", "TRUE"});
        fieldOptionMap.put("--optimize-interval", new String[]
                {"Interval between hyperprior optimizations ", "10", "train", "TRUE"});
        fieldOptionMap.put("--num-threads", new String[]
                {"Number of training threads ", "4", "train", "TRUE"});

        //// Input and Output Options ////

        fieldOptionMap.put("io-segment-files", new String[]
                {"Divide input into n-word chunks", "0", "io", "TRUE"});
        fieldOptionMap.put("io-metadata-delimiter", new String[]
                {"Metadata CSV delimiter", ",", "io", "TRUE"});
        fieldOptionMap.put("io-output-delimiter", new String[]
                {"Output CSV delimiter", ",", "io", "TRUE"});

        //// Disabled options ////

        // These two are disabled right now because I don't think they're
        // especially useful, and they're adding complexity to the interface.
        fieldOptionMap.put("--show-topics-interval", new String[]
                {"Topic preview interval", "100", "train", "FALSE"});
        fieldOptionMap.put("--doc-topics-threshold", new String[]
                {"Topic proportion threshold ", "0.0", "train", "FALSE"});

    }

    /**
     * Initializes the advanced controls.
     */
    public void initAdvControls() {
        for (String k:fieldOptionMap.keySet()) {
            if (k.equals("--stoplist-file")) {
                advFieldMap.put(k, stopFileField);
            } else {
                JTextField tempField = new JTextField(fieldOptionMap.get(k)[1]);
                advFieldMap.put(k, tempField);
            }
        }

        for (String k : checkBoxOptionMap.keySet()) {
            JCheckBox tempCheckBox = new JCheckBox(checkBoxOptionMap.get(k)[0]);
            if(checkBoxOptionMap.get(k)[1].equals("TRUE")) {
                tempCheckBox.setSelected(true);
            }

            advCheckBoxMap.put(k, tempCheckBox);

            if (k.equals("--remove-stopwords")) {
                stopBox = tempCheckBox;
            }
        }
    }

    /**
     * Reset advanced controls to default values.
     */
    public void resetAdvControls() {
        for (String k : fieldOptionMap.keySet()) {
            advFieldMap.get(k).setText(fieldOptionMap.get(k)[1]);
        }

        for (String k : checkBoxOptionMap.keySet()) {
            if (checkBoxOptionMap.get(k)[1].equals("TRUE")) {
                advCheckBoxMap.get(k).setSelected(true);
            } else {
                advCheckBoxMap.get(k).setSelected(false);
            }
        }

        metadataFileField.setText(DEFAULT_METADATA_FILE);
        stopFileField.setText(DEFAULT_STOPLIST_FILE);
    }

    /**
     * Gets the adv args.
     *
     * @return the adv args
     */
    public ArrayList<String> getAdvArgs(String key) {
        ArrayList<String> advArgs = new ArrayList<String>();

        for (String k : checkBoxOptionMap.keySet()) {
            if (checkBoxOptionMap.get(k)[2].equals(key)) {
                if (advCheckBoxMap.get(k).isSelected()) {
                    advArgs.add(k);
                }
            }
        }

        for (String k : fieldOptionMap.keySet()) {
            if (fieldOptionMap.get(k)[2].equals(key)) {
                String v = advFieldMap.get(k).getText();

                // MALLET displays one less word than specified. (Why?)
                if (k.equals("--num-top-words")) {
                    v = Integer.toString(Integer.parseInt(v) + 1);
                }

                // Skip --stoplist-file, which needs to be handled manually.
                if (k.equals("--stoplist-file")) {
                    continue;
                }

                advArgs.add(k);
                advArgs.add(v);
            }
        }

        return advArgs;
    }

    /**
     * Builds a file chooser widget and adds it to the given panel.
     */
    public void addChooserPanel(
        int mode, JTextField inputField, String buttonText,
        String buttonIcon, String chooserHeader, JPanel chooserPanel
    ) {

        JFileChooser chooser = new JFileChooser();
        allFileChoosers.add(chooser);

        chooser.setFileSelectionMode(mode);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        JButton button = new JButton(buttonText, createImageIcon(buttonIcon));
        button.addActionListener(
                new OpenButtonListener(chooser, inputField, chooserHeader));

        chooserPanel.add(inputField);
        chooserPanel.add(button);
    }

    /**
     * Builds the advanced panel.
     */
    public void buildAdvPanel() {
        advancedFrame = new JFrame("TopicModelingTool");
        advPanel = new JPanel(new BorderLayout());

        //// Checkbox Panel ////

        Box advCheckBoxPanel = new Box(BoxLayout.Y_AXIS);
        advCheckBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        advCheckBoxPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 5));
        for (JCheckBox tempCheckBox : advCheckBoxMap.values()) {
            advCheckBoxPanel.add(tempCheckBox);
            tempCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        //// Text Field Panel ////

        JPanel advFieldPanel = new JPanel(new GridLayout(0, 2));
        advFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        advFieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Iterator<JTextField> fieldIter = advFieldMap.values().iterator();
        for (String[] opts : fieldOptionMap.values()) {
            JTextField field = fieldIter.next();
            if (opts[3].equals("TRUE")) {
                advFieldPanel.add(new Label(opts[0]));
                advFieldPanel.add(field);
            }
        }

        //// Join Panels Into Box ////

        Box advBox = new Box(BoxLayout.Y_AXIS);
        advBox.add(advCheckBoxPanel);
        advBox.add(advFieldPanel);
        advPanel.add(advBox, BorderLayout.CENTER);

        //// File Choosers ////

        JPanel fcPanel = new JPanel(new GridLayout(2, 3));

        metadataFileField.setEnabled(true);
        metadataFileField.setEditable(false);
        metadataFileField.setText(DEFAULT_METADATA_FILE);
        addChooserPanel(
            JFileChooser.FILES_ONLY, metadataFileField, "Metadata File...",
            "/images/Open16.gif", "Metadata File", fcPanel
        );

        stopFileField.setEnabled(true);
        stopFileField.setEditable(false);
        stopFileField.setText(DEFAULT_STOPLIST_FILE);
        addChooserPanel(
            JFileChooser.FILES_ONLY, stopFileField, "Stopword File...",
            "/images/Open16.gif", "Stopword File", fcPanel
        );

        fcPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //// Buttons ////

        advPanel.add(fcPanel, BorderLayout.NORTH);
        JButton resetButton = new JButton("Default Options");
        resetButton.addActionListener(new ResetButtonListener());
        JPanel btmPanel = new JPanel();
        btmPanel.add(resetButton);
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                advancedFrame.setVisible(false);
            }
        });

        btmPanel.add(okButton);
        btmPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        //// Assemble Panel ////

        advPanel.add(btmPanel, BorderLayout.SOUTH);
        advPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        advancedFrame.getContentPane().add(advPanel);
        advancedFrame.setLocation(550, 100);
        advancedFrame.setSize(450, 300);
        advancedFrame.pack();
        advancedFrame.setResizable(false);
        advancedFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    /**
     * Go.
     */
    public void go() {
        log = new JTextArea(20, 20);
        log.setMargin(new Insets(5, 5, 5, 5));
        log.setEditable(false);

        redirectSystemStreams();

        JScrollPane logScrollPane = new JScrollPane(log);
        setDefaultOptions();
        initAdvControls();
        buildAdvPanel();

        //// Input File Chooser ////

        // TEMPORARILY, single input files have been disabled. There are
        // some bugs that make single input files hard to use; better
        // for now just to disable until support is solid.

        JPanel inoutPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inoutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        inputDirTfield.setEnabled(true);
        inputDirTfield.setEditable(false);
        inputDirTfield.setText(DEFAULT_INPUT_DIR);

        addChooserPanel(
            JFileChooser.DIRECTORIES_ONLY, inputDirTfield, "Input Dir...",
            "/images/Open16.gif", "Input Dir", inoutPanel
        );

        //// Output File Chooser ////

        outputDirTfield.setEnabled(true);
        outputDirTfield.setEditable(false);
        outputDirTfield.setText(DEFAULT_OUTPUT_DIR);
        addChooserPanel(
            JFileChooser.DIRECTORIES_ONLY, outputDirTfield, "Output Dir...",
            "/images/Open16.gif", "Output Dir", inoutPanel
        );

        //// Advanced Button and Number of Topics ////

        // It just occurred to me that calling these settings
        // "Advanced..." could be a form of microagression.
        advancedButton = new JButton("Optional Settings...");
        advancedButton.addActionListener(new AdvancedButtonListener());

        JPanel advancedPanel = new JPanel();
        advancedPanel.add(new Label("Number of topics:"));
        numTopics.setText("10");
        advancedPanel.add(numTopics);
        advancedPanel.add(advancedButton);

        //// Train Button ////

        trainButton = new JButton("<html><b>Learn Topics</b><html>", createImageIcon("/images/gears.png"));
        trainButton.addActionListener(new TrainButtonListener());

        JPanel trainPanel = new JPanel();
        trainPanel.add(trainButton);

        //// Button Box ////

        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        buttonBox.add(inoutPanel);
        buttonBox.add(advancedPanel);
        buttonBox.add(trainPanel);
        buttonBox.add(sep);

        //// Console ////

        Label cons = new Label("Console");
        cons.setAlignment(Label.CENTER);
        buttonBox.add(new JPanel().add(cons));

        //// Main Panel ////

        JPanel mainPanel = new JPanel(new BorderLayout());
        //Add the buttons and the log to this panel.
        mainPanel.add(buttonBox, BorderLayout.NORTH);

        clearButton = new JButton("Clear Console");
        clearButton.addActionListener(new ClearButtonListener());

        mainPanel.add(logScrollPane, BorderLayout.CENTER);
        mainPanel.add(clearButton, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //// Root Window ////

        rootframe = new JFrame("TopicModelingTool");
        rootframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        rootframe.addFocusListener(new FrameFocusListener());

        JComponent newContentPane = (JComponent) mainPanel;
        newContentPane.setOpaque(true); //content panes must be opaque
        rootframe.setContentPane(newContentPane);
        rootframe.setLocation(500, 100);
        rootframe.pack();

        createHelp1();
        createHelp2();
        resetAdvControls();
        runMalletCleanup();

        rootframe.setVisible(true);
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
        Path outputDirPath = Paths.get(getOutputDirName());
        Path segmentPath = Paths.get(getOutputDirName(), "segments");
        Path newMetadataPath = Paths.get(getOutputDirName(), "segments-metadata.csv");
        Path metadataPath = null;

        if (getMetadataFileName().equals("")) {
            metadataPath = Paths.get(getOutputDirName(), "autogen-metadata.csv");
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
        String outputDir = getOutputDirName();
        String collectionPath = null;

        try {
            collectionPath =
                new File(outputDir, MALLET_TOPIC_INPUT).getCanonicalPath();
        } catch (IOException exc) {
            errorLog(exc);
            runMalletCleanup();
            return;
        }

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

        outputDir = getOutputDirName();
        String stateFile = outputDir + File.separator + MALLET_STATE_GZ;
        String topicKeysFile = outputDir + File.separator + MALLET_TOPIC_KEYS;
        String outputDocTopicsFile = outputDir + File.separator + MALLET_DOC_TOPICS;
        String wordsTopicCountsFile = outputDir + File.separator + MALLET_WORDS_TOPICS_COUNTS;

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
            GunZipper g = new GunZipper(new File(stateFile));
            g.unzip(new File(outputDir + File.separator + MALLET_STATE));

            delim = advFieldMap.get("io-metadata-delimiter").getText();
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
            appendLog("Mallet Output files written in " + outputDir);
        }
        if (advCheckBoxMap.get("io-generate-html").isSelected()) {
            appendLog("Html Output files written in " + outputDir + File.separator + "output_html");
        }

        appendLog("Csv Output files written in " + outputDir + File.separator + "output_csv");

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
        String[] fileNames = new String[6];
        fileNames[0] = MALLET_TOPIC_INPUT;
        fileNames[1] = MALLET_TOPIC_KEYS;
        fileNames[2] = MALLET_STATE;
        fileNames[3] = MALLET_STATE_GZ;
        fileNames[4] = MALLET_DOC_TOPICS;
        fileNames[5] = MALLET_WORDS_TOPICS_COUNTS;

        for (String f:fileNames) {
            if (!(new File(outputDir, f).canWrite())) {
                appendLog("clearExtrafiles failed on ");
                appendLog(f);
            }
            Boolean b = new File(outputDir, f).delete();
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args, boolean istest) {

        if (args.length > 0) {
            DEFAULT_INPUT_DIR = args[0];
        }

        if (args.length > 1) {
            DEFAULT_OUTPUT_DIR = args[1];
        } else {
            String home = System.getProperty("user.home");
            try {
                DEFAULT_OUTPUT_DIR = new File(home).getCanonicalPath();
            } catch (IOException ioe) {
                DEFAULT_OUTPUT_DIR = new File(home).getAbsolutePath();
            }
        }

        if (args.length > 2) {
            DEFAULT_METADATA_FILE = args[2];
        }

        TopicModelingTool tmt = new TopicModelingTool(istest);
        tmt.go();

        if (istest) {
            tmt.runMallet();
        }
    }

    public static void main(String[] args) {
        TopicModelingTool.main(args, false);
    }
}
