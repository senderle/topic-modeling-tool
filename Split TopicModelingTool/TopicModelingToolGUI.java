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
import TopicModelingToolController;
import TopicModelingToolAccessor;

public class TopicModelingToolGUI {
    /** used for testing to set an input dir on startup */
    public static String DEFAULT_INPUT_DIR = "";
    public static String DEFAULT_OUTPUT_DIR = "";
    public static String DEFAULT_METADATA_FILE = "";
    public static String DEFAULT_STOPLIST_FILE = "";

    private JFrame rootframe; 
    private JFrame advancedFrame;
    private JPanel mainPanel;
    private JPanel advPanel;

    private JDialog helpPane1;
    private JDialog helpPane2;

    private JButton inputDataButton;
    private JButton outputDirButton;
    private JButton trainButton;
    private JButton clearButton;
    private JButton advancedButton;
    private JCheckBox stopBox;

    private JTextField numTopics;

    private ArrayList<JFileChooser> allFileChoosers;

    private LinkedHashMap<String, OptionStrings> checkBoxOptionMap;
    private LinkedHashMap<String, OptionStrings> fieldOptionMap;

    private LinkedHashMap<String, JCheckBox> advCheckBoxMap;
    private LinkedHashMap<String, JTextField> advFieldMap;

    private Boolean frameBusy;

    private TopicModelingToolController controller;
    private TopicModelingToolAccessor accessor;

    public TopicModelingToolGUI() {
        this.rootframe = null;
        this.advancedFrame = null;
        this.mainPanel = null;
        this.advPanel = null;
        this.helpPane1 = null;
        this.helpPane2 = null;
        this.inputDataButton = null;
        this.outputDirButton = null;
        this.trainButton = null;
        this.clearButton = null;
        this.advancedButton = null;
        this.stopBox = null;
        this.numTopics = new JTextField(2);
        this.allFileChoosers = new ArrayList<JFileChooser>();
        this.checkBoxOptionMap = new LinkedHashMap<String, OptionStrings>();
        this.fieldOptionMap = new LinkedHashMap<String, OptionStrings>();
        this.advCheckBoxMap = new LinkedHashMap<String, JCheckBox>();
        this.advFieldMap = new LinkedHashMap<String, JTextField>();
        this.frameBusy = false;
        this.accessor = new TopicModelingToolAccessor();
        this.controller = new TopicModelingToolController(this.accessor, this);
    }


    /**
     * Creates the help panel in the Basic window.
     */
    public void createHelp1() {

        this.helpPane1 = new JDialog();
        JPanel p1 = new JPanel();
        String text = "<html><b>Input - </b>Select a directory containing text files," + 
                      " or a single text file where each line is a"+
                      " data instance.<br><br> <b>Output Directory -</b> All generated output" +
                      " is written to this folder. Current directory by default.<br><br>"+
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
        this.helpPane1.setContentPane(p1);
        this.helpPane1.setTitle("Basic Options");
        this.helpPane1.setResizable(false);
        this.helpPane1.pack();
        this.helpPane1.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    /**
     * Show help in Basic panel.
     */
    public void showHelp1() {
        this.helpPane1.setVisible(true);
    }

    /**
     * Show help in Advanced panel.
     */
    public void showHelp2() {
        this.helpPane2.setVisible(true);
    }

    /**
     * Creates the help panel in the Advanced window.
     */
    public void createHelp2() {
        this.helpPane2 = new JDialog();
        JPanel p1 = new JPanel();

        String text = "<html><b>Remove stopwords - </b>If checked, remove a " +
                      "list of \"stop words\" from the text.<br><br>"+
                      " <b>Stopword file - </b>Read \"stop words\" from a file, one per line. "+
                      "Default is Mallet's list of standard English stopwords.<br><br>"+
                      "<b>Case sensitive - </b>If checked, do not force all strings to "+
                      "lowercase.<br><br><b>No. of iterations - </b> The number of iterations of "+
                      "Gibbs sampling to run. Default is 1000.<br><br>" +
                      "<b>No. of topic words printed - </b>The number of most probable words to "+
                      "print for each topic after model estimation.<br><br>"+
                      "<b>Topic proportion threshold - </b>Do not print topics with proportions "+
                      "less than this threshold value.</b></html>";

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
        this.helpPane2.setContentPane(p1);
        this.helpPane2.setTitle("Advanced Options");
        this.helpPane2.setResizable(false);
        this.helpPane2.pack();
        this.helpPane2.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    /**
     * The listener interface for receiving openButton events. The same interface is used for both 
     * the input
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
            int returnVal = filechooser.showOpenDialog(this.mainPanel);

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

                this.accessor.appendLog("Chose " + filedescription + inputType + inputDir);

                filefield.setText(inputDir);

                for (JFileChooser chooser : this.allFileChoosers) {
                    chooser.setCurrentDirectory(file.getParentFile());
                }

            } else {
                this.accessor.appendLog("Open command cancelled by user.");
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
           if(this.frameBusy){
               Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
               this.rootframe.setCursor(hourglassCursor);
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
            this.advancedFrame.setVisible(true);

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
                    if (this.accessor.getInputDirName().equals("")) {
                        JOptionPane.showMessageDialog(this.mainPanel, 
                            "Please select an input file or directory", 
                            "Invalid input", JOptionPane.ERROR_MESSAGE);
                    } else {
                        this.controller.runMallet(this.fieldOptionMap, this.checkBoxOptionMap, 
                            this.advFieldMap, this.advCheckBoxMap);
                    }
                }
            };
            t.start();
        }

    }

    public void updateStatusCursor(String statusMessage) {
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        this.rootframe.setCursor(hourglassCursor);
        this.frameBusy = true;
        this.trainButton.setText(statusMessage);
    }

    /**
     * Clear console area
     */
    public class ClearButtonListener implements ActionListener {

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            this.accessor.setLog("");
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
     * Set corresponding special string, default value, description and associated command for the 
     * options
     */
    public void setDefaultOptions() {
        // Field Format:
        // widgetMap.put("--MALLET-OPTION-or-io-key", new String[]
        //      {"Widget Label", "Default Value", "Widget Category", "Autogenerate Widget?"});

        //// Nonstandard options ////

        // (These are manually generated and appear at the top of the
        // advanced window).
        this.fieldOptionMap.put("io-metadata", new OptionStrings("Metadata File", 
            DEFAULT_METADATA_FILE, "io", false));

        this.fieldOptionMap.put("--stoplist-file", new OptionStrings("Custom Stoplist File", 
            DEFAULT_STOPLIST_FILE, "import", false));

        //// Checkboxes ////

        this.checkBoxOptionMap.put("--remove-stopwords", 
            new OptionStrings("Remove default English stopwords", 
            "TRUE", "import", true));
        this.checkBoxOptionMap.put("--preserve-case", new OptionStrings("Preserve case ", 
            "FALSE", "import", true));
        this.checkBoxOptionMap.put("io-generate-html", new OptionStrings("Generate HTML output", 
            "TRUE", "io", true));
        this.checkBoxOptionMap.put("io-preserve-mallet", 
            new OptionStrings("Preserve raw MALLET output", 
            "FALSE", "io", true));

        //// Importing field options ////

        // This regex accepts all unicode characters.
        this.fieldOptionMap.put("--token-regex", 
            new OptionStrings("Tokenize with regular expression", 
            "[\\p{L}\\p{N}_]+", "import", true));

        //// Training field options ////

        this.fieldOptionMap.put("--num-iterations", new OptionStrings("Number of iterations ", 
            "400", "train", true));
        this.fieldOptionMap.put("--num-top-words", 
            new OptionStrings("Number of topic words to print ", 
            "20", "train", true));
        this.fieldOptionMap.put("--optimize-interval", 
            new OptionStrings("Interval between hyperprior optimizations ", 
            "10", "train", true));
        this.fieldOptionMap.put("--num-threads", new OptionStrings("Number of training threads ", 
            "4", "train", true));

        //// Input and Output Options ////

        this.fieldOptionMap.put("io-segment-files", 
            new OptionStrings("Divide input into n-word chunks", 
            "0", "io", true));
        this.fieldOptionMap.put("io-metadata-delimiter", 
            new OptionStrings("Metadata CSV delimiter", ",", "io", true));
        this.fieldOptionMap.put("io-output-delimiter", new OptionStrings("Output CSV delimiter", 
            ",", "io", true));

        //// Disabled options ////

        // These two are disabled right now because I don't think they're
        // especially useful, and they're adding complexity to the interface.
        this.fieldOptionMap.put("--show-topics-interval", 
            new OptionStrings("Topic preview interval", "100", "train", false));
        this.fieldOptionMap.put("--doc-topics-threshold", 
            new OptionStrings("Topic proportion threshold ", "0.0", "train", false));

    }

    /**
     * Initializes the advanced controls.
     */
    public void initAdvControls() {
        for (String k : this.fieldOptionMap.keySet()) {
            if (k.equals("--stoplist-file")) {
                this.advFieldMap.put(k, this.accessor.getStopFileField());
            } else {
                JTextField tempField = new JTextField(this.fieldOptionMap.get(k).getOptionB());
                this.advFieldMap.put(k, tempField);
            }
        }

        for (String k : this.checkBoxOptionMap.keySet()) {
            JCheckBox tempCheckBox = new JCheckBox(this.checkBoxOptionMap.get(k).getOptionA());
            if(this.checkBoxOptionMap.get(k).getOptionB().equals("TRUE")) {
                tempCheckBox.setSelected(true);
            }

            this.advCheckBoxMap.put(k, tempCheckBox);

            if (k.equals("--remove-stopwords")) {
                this.stopBox = tempCheckBox;
            }
        }
    }

    /**
     * Reset advanced controls to default values.
     */
    public void resetAdvControls() {
        for (String k : this.fieldOptionMap.keySet()) {
            this.advFieldMap.get(k).setText(this.fieldOptionMap.get(k).getOptionB());
        }

        for (String k : this.checkBoxOptionMap.keySet()) {
            if (this.checkBoxOptionMap.get(k).getOptionB().equals("TRUE")) {
                this.advCheckBoxMap.get(k).setSelected(true);
            } else {
                this.advCheckBoxMap.get(k).setSelected(false);
            }
        }

        this.accessor.setMetadataFileField(DEFAULT_METADATA_FILE);
        this.accessor.setStopFileField(DEFAULT_STOPLIST_FILE);
    }

    /**
     * Gets the adv args.
     *
     * @return the adv args
     */
    public ArrayList<String> getAdvArgs(String key) {
        ArrayList<String> advArgs = new ArrayList<String>();

        for (String k : this.checkBoxOptionMap.keySet()) {
            if (this.checkBoxOptionMap.get(k).getOptionC().equals(key)) {
                if (this.advCheckBoxMap.get(k).isSelected()) {
                    advArgs.add(k);
                }
            }
        }

        for (String k : this.fieldOptionMap.keySet()) {
            if (this.fieldOptionMap.get(k).getOptionC().equals(key)) {
                String v = this.advFieldMap.get(k).getText();

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
        this.allFileChoosers.add(chooser);
        chooser.setFileSelectionMode(mode);

        Path home = this.accessor.getUserHomePath();
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
        this.advancedFrame = new JFrame("TopicModelingTool");
        this.advPanel = new JPanel(new BorderLayout());

        //// Checkbox Panel ////

        Box advCheckBoxPanel = new Box(BoxLayout.Y_AXIS);
        advCheckBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        advCheckBoxPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 5));
        for (JCheckBox tempCheckBox : this.advCheckBoxMap.values()) {
            advCheckBoxPanel.add(tempCheckBox);
            tempCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        //// Text Field Panel ////

        JPanel advFieldPanel = new JPanel(new GridLayout(0, 2));
        advFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        advFieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Iterator<JTextField> fieldIter = this.advFieldMap.values().iterator();
        for (String[] opts : this.fieldOptionMap.values()) {
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
        this.advPanel.add(advBox, BorderLayout.CENTER);

        //// File Choosers ////

        JPanel fcPanel = new JPanel(new GridLayout(2, 3));

        this.accessor.enableMetadataFileField(true);
        this.accessor.setEditableMetadataFileField(false);
        this.accessor.setMetadataFileField(DEFAULT_METADATA_FILE);
        addChooserPanel(
            JFileChooser.FILES_ONLY, this.accessor.getMetadataFileField(), "Metadata File...",
            "/images/Open16.gif", "Metadata File", fcPanel
        );

        this.accessor.enableStopFileField(true);
        this.accessor.setEditableStopFileField(false);
        this.accessor.setStopFileField(DEFAULT_STOPLIST_FILE);
        addChooserPanel(
            JFileChooser.FILES_ONLY, this.accessor.getStopFileField(), "Stopword File...",
            "/images/Open16.gif", "Stopword File", fcPanel
        );

        fcPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //// Buttons ////

        this.advPanel.add(fcPanel, BorderLayout.NORTH);
        JButton resetButton = new JButton("Default Options");
        resetButton.addActionListener(new ResetButtonListener());
        JPanel btmPanel = new JPanel();
        btmPanel.add(resetButton);
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                this.advancedFrame.setVisible(false);
            }
        });

        btmPanel.add(okButton);
        btmPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        //// Assemble Panel ////

        this.advPanel.add(btmPanel, BorderLayout.SOUTH);
        this.advPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.advancedFrame.getContentPane().add(this.advPanel);
        this.advancedFrame.setLocation(550, 100);
        this.advancedFrame.setSize(450, 300);
        this.advancedFrame.pack();
        this.advancedFrame.setResizable(false);
        this.advancedFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    /**
     * Go.
     */
    public void go() {
        this.accessor.setUpNewLog();

        this.accessor.redirectSystemStreams();

        JScrollPane logScrollPane = new JScrollPane(this.accessor.getLog);
        setDefaultOptions();
        initAdvControls();
        buildAdvPanel();

        //// Input File Chooser ////

        // TEMPORARILY, single input files have been disabled. There are
        // some bugs that make single input files hard to use; better
        // for now just to disable until support is solid.

        JPanel inoutPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inoutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.accessor.enableInputDirTfield(true);
        this.accessor.setEditableInputDirTfield(false);
        this.accessor.setInputDirTfield(DEFAULT_INPUT_DIR);

        addChooserPanel(
            JFileChooser.DIRECTORIES_ONLY, this.accessor.getInputDirTfield(), "Input Dir...",
            "/images/Open16.gif", "Input Dir", inoutPanel
        );

        //// Output File Chooser ////

        this.accessor.enableOutputDirTfield(true);
        this.accessor.setEditableOutputDirTfield(false);
        this.accessor.setOutputDirTfield(DEFAULT_OUTPUT_DIR);
        addChooserPanel(
            JFileChooser.DIRECTORIES_ONLY, this.accessor.getOutputDirTfield(), "Output Dir...",
            "/images/Open16.gif", "Output Dir", inoutPanel
        );

        //// Advanced Button and Number of Topics ////

        // It just occurred to me that calling these settings
        // "Advanced..." could be a form of microagression.
        this.advancedButton = new JButton("Optional Settings...");
        this.advancedButton.addActionListener(new AdvancedButtonListener());

        JPanel advancedPanel = new JPanel();
        advancedPanel.add(new Label("Number of topics:"));
        this.numTopics.setText("10");
        advancedPanel.add(this.numTopics);
        advancedPanel.add(this.advancedButton);

        //// Train Button ////

        this.trainButton = new JButton("<html><b>Learn Topics</b><html>", 
            createImageIcon("/images/gears.png"));
        this.trainButton.addActionListener(new TrainButtonListener());

        JPanel trainPanel = new JPanel();
        trainPanel.add(this.trainButton);

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

        this.mainPanel = new JPanel(new BorderLayout());
        //JPanel mainPanel = new JPanel(new BorderLayout());
        //Add the buttons and the log to this panel.
        this.mainPanel.add(buttonBox, BorderLayout.NORTH);

        this.clearButton = new JButton("Clear Console");
        this.clearButton.addActionListener(new ClearButtonListener());

        this.mainPanel.add(logScrollPane, BorderLayout.CENTER);
        this.mainPanel.add(this.clearButton, BorderLayout.SOUTH);
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //// Root Window ////

        this.rootframe = new JFrame("TopicModelingTool");
        this.rootframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.rootframe.addFocusListener(new FrameFocusListener());

        JComponent newContentPane = (JComponent) this.mainPanel;
        newContentPane.setOpaque(true); //content panes must be opaque
        this.rootframe.setContentPane(newContentPane);
        this.rootframe.setLocation(500, 100);
        this.rootframe.pack();

        createHelp1();
        createHelp2();
        resetAdvControls();
        this.controller.runMalletCleanup();

        this.rootframe.setVisible(true);
    }

    private JFrame getRootFrame() {
        return this.rootframe;
    }

    private void setRootFrame(Cursor cursor) {
        this.rootframe.setCursor(cursor);
    }

    private void enableClearButton(boolean input) {
        this.clearButton.setEnabled(input);
    }

    private void enableTrainButton(boolean input) {
        this.trainButton.setEnabled(input);
    }

    private void setTrainButton(String input) {
        this.trainButton.setText(input);
    }

    private void setFrameBusy(boolean input) {
        this.frameBusy = input;
    }

    private JTextField getNumTopics() {
        return this.numTopics;
    }

    private LinkedHashMap<String, OptionStrings> getCheckBoxOptionMap() {
        return this.getCheckBoxOptionMap;
    }

    private LinkedHashMap<String, OptionStrings> getFieldOptionMap() {
        return this.fieldOptionMap;
    }

    private LinkedHashMap<String, JTextField> getAdvFieldMap() {
        return this.advFieldMap;
    }

    private LinkedHashMap<String, JCheckBox> getAdvCheckBoxMap() {
        return this.advCheckBoxMap;
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
            DEFAULT_OUTPUT_DIR = getUserHomePath().toString();
        }

        if (args.length > 2) {
            DEFAULT_METADATA_FILE = args[2];
        }

        TopicModelingToolGUI tmt = new TopicModelingToolGUI(istest);
        tmt.go();

        if (istest) {
            tmt.controller.runMallet(this.getCheckBoxOptionMap(), this.getFieldOptionMap(), 
                this.getAdvFieldMap(), this.getAdvCheckBoxMap());
        }
    }

    public static void main(String[] args) {
        TopicModelingToolGUI.main(args, false);
    }
}