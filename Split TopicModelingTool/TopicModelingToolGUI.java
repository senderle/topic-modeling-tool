package cc.mallet.topics.gui;

import java.awt.*;
import java.awt.event.*;

import java.nio.file.Path;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;

import OptionStrings;

/**
 * The Class TopicModelingGUI.
 */
public class TopicModelingToolGUI {
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

    LinkedHashMap<String, OptionStrings> checkBoxOptionMap = new LinkedHashMap<String, OptionStrings>();
    LinkedHashMap<String, OptionStrings> fieldOptionMap = new LinkedHashMap<String, OptionStrings>();

    LinkedHashMap<String, JCheckBox> advCheckBoxMap = new LinkedHashMap<String, JCheckBox>();
    LinkedHashMap<String, JTextField> advFieldMap = new LinkedHashMap<String, JTextField>();

    Boolean frameBusy = false;
    Boolean failOnExc = false;

    public TopicModelingTool(boolean isTest) {
        failOnExc = isTest;
    }

    public TopicModelingTool() {
        this(false);
    }

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
        fieldOptionMap.put("io-metadata", new OptionStrings("Metadata File", 
            DEFAULT_METADATA_FILE, "io", "FALSE"));

        fieldOptionMap.put("--stoplist-file", new OptionStrings("Custom Stoplist File", 
            DEFAULT_STOPLIST_FILE, "import", "FALSE"));

        //// Checkboxes ////

        checkBoxOptionMap.put("--remove-stopwords", new OptionStrings("Remove default English stopwords", 
            "TRUE", "import", "TRUE"));
        checkBoxOptionMap.put("--preserve-case", new OptionStrings("Preserve case ", 
            "FALSE", "import", "TRUE"));
        checkBoxOptionMap.put("io-generate-html", new OptionStrings("Generate HTML output", 
            "TRUE", "io", "TRUE"));
        checkBoxOptionMap.put("io-preserve-mallet", new OptionStrings("Preserve raw MALLET output", 
            "FALSE", "io", "TRUE"));

        //// Importing field options ////

        // This regex accepts all unicode characters.
        fieldOptionMap.put("--token-regex", new OptionStrings("Tokenize with regular expression", 
            "[\\p{L}\\p{N}_]+", "import", "TRUE"));

        //// Training field options ////

        fieldOptionMap.put("--num-iterations", new OptionStrings("Number of iterations ", 
            "400", "train", "TRUE"));
        fieldOptionMap.put("--num-top-words", new OptionStrings("Number of topic words to print ", 
            "20", "train", "TRUE"));
        fieldOptionMap.put("--optimize-interval", new OptionStrings("Interval between hyperprior optimizations ", 
            "10", "train", "TRUE"));
        fieldOptionMap.put("--num-threads", new OptionStrings("Number of training threads ", 
            "4", "train", "TRUE"));

        //// Input and Output Options ////

        fieldOptionMap.put("io-segment-files", new OptionStrings("Divide input into n-word chunks", 
            "0", "io", "TRUE"));
        fieldOptionMap.put("io-metadata-delimiter", new OptionStrings("Metadata CSV delimiter", 
            ",", "io", "TRUE"));
        fieldOptionMap.put("io-output-delimiter", new OptionStrings("Output CSV delimiter", 
            ",", "io", "TRUE"));

        //// Disabled options ////

        // These two are disabled right now because I don't think they're
        // especially useful, and they're adding complexity to the interface.
        fieldOptionMap.put("--show-topics-interval", new OptionStrings("Topic preview interval", 
            "100", "train", "FALSE"));
        fieldOptionMap.put("--doc-topics-threshold", new OptionStrings("Topic proportion threshold ", 
            "0.0", "train", "FALSE"));

    }

    /**
     * Initializes the advanced controls.
     */
    public void initAdvControls() {
        for (String k : fieldOptionMap.keySet()) {
            if (k.equals("--stoplist-file")) {
                advFieldMap.put(k, stopFileField);
            } else {
                JTextField tempField = new JTextField(fieldOptionMap.get(k).getOptionStringB());
                advFieldMap.put(k, tempField);
            }
        }

        for (String k : checkBoxOptionMap.keySet()) {
            JCheckBox tempCheckBox = new JCheckBox(checkBoxOptionMap.get(k).getOptionStringA());
            if(checkBoxOptionMap.get(k).getOptionStringB().equals("TRUE")) {
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
            advFieldMap.get(k).setText(fieldOptionMap.get(k).getOptionStringB());
        }

        for (String k : checkBoxOptionMap.keySet()) {
            if (checkBoxOptionMap.get(k).getOptionStringB().equals("TRUE")) {
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
            if (checkBoxOptionMap.get(k).getOptionStringC().equals(key)) {
                if (advCheckBoxMap.get(k).isSelected()) {
                    advArgs.add(k);
                }
            }
        }

        for (String k : fieldOptionMap.keySet()) {
            if (fieldOptionMap.get(k).getOptionStringC().equals(key)) {
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

        Path home = getUserHomePath();
        chooser.setCurrentDirectory(home.toFile());

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
}