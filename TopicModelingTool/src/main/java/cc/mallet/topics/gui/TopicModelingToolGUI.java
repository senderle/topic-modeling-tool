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
import java.lang.*;

import cc.mallet.topics.gui.Option;
import cc.mallet.topics.gui.TopicModelingToolController;
import cc.mallet.topics.gui.TopicModelingToolAccessor;

import cc.mallet.topics.gui.listeners.*;

public class TopicModelingToolGUI {
    /** used for testing to set an input dir on startup */
    public static String DEFAULT_INPUT_DIR = "";
    public static String DEFAULT_OUTPUT_DIR = "";
    public static String DEFAULT_METADATA_FILE = "";
    public static String DEFAULT_STOPLIST_FILE = "";

    private JFrame rootframe; 
    public JFrame advancedFrame;
    private JPanel mainPanel;
    private JPanel advPanel;

    private JDialog helpPane1;
    private JDialog helpPane2;

    private JButton trainButton;
    private JButton clearButton;
    private JButton advancedButton;
    private JCheckBox stopBox;

    private JTextField numTopics;

    private ArrayList<JFileChooser> allFileChoosers;

    private LinkedHashMap<String, Option<Boolean>> checkBoxOptionMap;
    private LinkedHashMap<String, Option<String>> fieldOptionMap;

    private LinkedHashMap<String, JCheckBox> advCheckBoxMap;
    private LinkedHashMap<String, JTextField> advFieldMap;

    private Boolean frameBusy;

    private TopicModelingToolController controller;
    private TopicModelingToolAccessor accessor;

    public TopicModelingToolGUI(boolean isTest) {
        this.rootframe = null;
        advancedFrame = null;
        this.mainPanel = null;
        this.advPanel = null;
        this.helpPane1 = null;
        this.helpPane2 = null;
        this.trainButton = null;
        this.clearButton = null;
        this.advancedButton = null;
        this.stopBox = null;
        this.numTopics = new JTextField(2);
        this.allFileChoosers = new ArrayList<JFileChooser>();
        this.checkBoxOptionMap = new LinkedHashMap<String, Option<Boolean>>();
        this.fieldOptionMap = new LinkedHashMap<String, Option<String>>();
        this.advCheckBoxMap = new LinkedHashMap<String, JCheckBox>();
        this.advFieldMap = new LinkedHashMap<String, JTextField>();
        this.frameBusy = false;
        this.accessor = new TopicModelingToolAccessor(isTest);
        this.controller = new TopicModelingToolController(this.accessor, this);
    }

    public TopicModelingToolGUI() {
        this(false);
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

    public void updateStatusCursor(String statusMessage) {
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        this.rootframe.setCursor(hourglassCursor);
        this.frameBusy = true;
        this.trainButton.setText(statusMessage);
    }

    
    /**
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path the path
     * @return the image icon
     */
    protected static ImageIcon createImageIcon(String path) {
        // Switched from TopicModelingTool for TopicModelingToolGUI
        // Let's see how this will work
        java.net.URL imgURL = TopicModelingToolGUI.class.getResource(path);
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
        //

        //// NOTE: Options will be displayed in the order they are added here.

        //// Nonstandard options ////

        // (These are manually generated and appear at the top of the
        // advanced window).
        this.fieldOptionMap.put("io-metadata", new Option<String>("Metadata File", 
            DEFAULT_METADATA_FILE, "io", false));

        this.fieldOptionMap.put("--stoplist-file", new Option<String>("Custom Stoplist File", 
            DEFAULT_STOPLIST_FILE, "import", false));

        //// Checkboxes ////

        this.checkBoxOptionMap.put("--remove-stopwords", 
            new Option<Boolean>("Remove default English stopwords", 
            true, "import", true));
        this.checkBoxOptionMap.put("--preserve-case", new Option<Boolean>("Preserve case ", 
            false, "import", true));
        this.checkBoxOptionMap.put("io-generate-html", new Option<Boolean>("Generate HTML output", 
            true, "io", true));
        this.checkBoxOptionMap.put("io-preserve-mallet", 
            new Option<Boolean>("Preserve raw MALLET output", 
            false, "io", true));

        //// Importing field options ////

        // This regex accepts all unicode characters.
        this.fieldOptionMap.put("--token-regex", 
            new Option<String>("Tokenize with regular expression", 
            "[\\p{L}\\p{N}_]+", "import", true));

        //// Training field options ////

        this.fieldOptionMap.put("--num-iterations", new Option<String>("Number of iterations ", 
            "400", "train", true));
        this.fieldOptionMap.put("--num-top-words", 
            new Option<String>("Number of topic words to print ", 
            "20", "train", true));
        this.fieldOptionMap.put("--optimize-interval", 
            new Option<String>("Alpha & Beta optimization frequency ", 
            "10", "train", true));
        this.fieldOptionMap.put("--alpha", 
            new Option<String>("Topic density parameter (Alpha) ", 
            "50", "train", true));
        this.fieldOptionMap.put("--beta", 
            new Option<String>("Word density parameter (Beta) ", 
            "0.01", "train", true));
        this.fieldOptionMap.put("--num-threads", new Option<String>("Number of training threads ", 
            "4", "train", true));

        //// Input and Output Options ////

        this.fieldOptionMap.put("io-segment-files", 
            new Option<String>("Divide input into n-word chunks", 
            "0", "io", true));
        this.fieldOptionMap.put("io-metadata-delimiter", 
            new Option<String>("Metadata CSV delimiter", ",", "io", true));
        this.fieldOptionMap.put("io-output-delimiter", new Option<String>("Output CSV delimiter", 
            ",", "io", true));

        //// Disabled options ////

        // These two are disabled right now because I don't think they're
        // especially useful, and they're adding complexity to the interface.
        this.fieldOptionMap.put("--show-topics-interval", 
            new Option<String>("Topic preview interval", "100", "train", false));
        this.fieldOptionMap.put("--doc-topics-threshold", 
            new Option<String>("Topic proportion threshold ", "0.0", "train", false));

    }

    /**
     * Initializes the advanced controls.
     */
    public void initAdvControls() {
        for (String k : this.fieldOptionMap.keySet()) {
            if (k.equals("--stoplist-file")) {
                this.advFieldMap.put(k, this.accessor.getStopFileField());
            } else {
                JTextField tempField = new JTextField(this.fieldOptionMap.get(k).getDefaultVal());
                this.advFieldMap.put(k, tempField);
            }
        }

        for (String k : this.checkBoxOptionMap.keySet()) {
            JCheckBox tempCheckBox = new JCheckBox(this.checkBoxOptionMap.get(k).getDescription());
            if(this.checkBoxOptionMap.get(k).getDefaultVal()) {
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
            this.advFieldMap.get(k).setText(this.fieldOptionMap.get(k).getDefaultVal());
        }

        for (String k : this.checkBoxOptionMap.keySet()) {
            if (this.checkBoxOptionMap.get(k).getDefaultVal()) {
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
            if (this.checkBoxOptionMap.get(k).getCategory().equals(key)) {
                if (this.advCheckBoxMap.get(k).isSelected()) {
                    advArgs.add(k);
                }
            }
        }

        for (String k : this.fieldOptionMap.keySet()) {
            if (this.fieldOptionMap.get(k).getCategory().equals(key)) {
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
                new OpenButtonListener(chooser, inputField, chooserHeader, this, this.accessor));

        chooserPanel.add(inputField);
        chooserPanel.add(button);
    }

    /**
     * Builds the advanced panel.
     */
    public void buildAdvPanel() {
        advancedFrame = new JFrame("TopicModelingTool");
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
        for (Option<String> opts : this.fieldOptionMap.values()) {
            JTextField field = fieldIter.next();
            if (opts.getAutogenerate()) {
                advFieldPanel.add(new Label(opts.getDescription()));
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
        resetButton.addActionListener(new ResetButtonListener(this));
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

        this.advPanel.add(btmPanel, BorderLayout.SOUTH);
        this.advPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        advancedFrame.getContentPane().add(this.advPanel);
        advancedFrame.setLocation(550, 100);
        advancedFrame.setSize(450, 300);
        advancedFrame.pack();
        advancedFrame.setResizable(false);
        advancedFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void runMallet(LinkedHashMap<String, Option<Boolean>> checkBoxOptionMap, 
                          LinkedHashMap<String, Option<String>> fieldOptionMap,
                          LinkedHashMap<String, JTextField> advFieldMap,
                          LinkedHashMap<String, JCheckBox> advCheckBoxMap) {
        this.controller.runMallet(checkBoxOptionMap, fieldOptionMap, advFieldMap, advCheckBoxMap);
    }

    /**
     * Go.
     */
    public void go() {
        this.accessor.setUpNewLog();

        this.accessor.redirectSystemStreams();

        JScrollPane logScrollPane = new JScrollPane(this.accessor.getLog());
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
        this.advancedButton.addActionListener(new AdvancedButtonListener(this));

        JPanel advancedPanel = new JPanel();
        advancedPanel.add(new Label("Number of topics:"));
        this.numTopics.setText("10");
        advancedPanel.add(this.numTopics);
        advancedPanel.add(this.advancedButton);

        //// Train Button ////

        this.trainButton = new JButton("<html><b>Learn Topics</b><html>", 
            createImageIcon("/images/gears.png"));
        this.trainButton.addActionListener(new TrainButtonListener(this.controller, this.accessor, this));

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
        this.clearButton.addActionListener(new ClearButtonListener(this.accessor));

        this.mainPanel.add(logScrollPane, BorderLayout.CENTER);
        this.mainPanel.add(this.clearButton, BorderLayout.SOUTH);
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //// Root Window ////

        this.rootframe = new JFrame("TopicModelingTool");
        this.rootframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.rootframe.addFocusListener(new FrameFocusListener(this));

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

    public JFrame getRootFrame() {
        return this.rootframe;
    }

    public void setRootFrame(Cursor cursor) {
        this.rootframe.setCursor(cursor);
    }

    public void setAdvancedFrame(boolean input) {
        advancedFrame.setVisible(input);
    }

    public void enableClearButton(boolean input) {
        this.clearButton.setEnabled(input);
    }

    public void enableTrainButton(boolean input) {
        this.trainButton.setEnabled(input);
    }

    public void setTrainButton(String input) {
        this.trainButton.setText(input);
    }

    public void setFrameBusy(boolean input) {
        this.frameBusy = input;
    }

    public boolean getFrameBusy() {
        return this.frameBusy;
    }

    public JTextField getNumTopics() {
        return this.numTopics;
    }

    public LinkedHashMap<String, Option<Boolean>> getCheckBoxOptionMap() {
        return this.checkBoxOptionMap;
    }

    public LinkedHashMap<String, Option<String>> getFieldOptionMap() {
        return this.fieldOptionMap;
    }

    public LinkedHashMap<String, JTextField> getAdvFieldMap() {
        return this.advFieldMap;
    }

    public LinkedHashMap<String, JCheckBox> getAdvCheckBoxMap() {
        return this.advCheckBoxMap;
    }

    public JPanel getMainPanel() {
        return this.mainPanel;
    }

    public void setChoosers(File filer) {
        for (JFileChooser chooser : this.allFileChoosers) {
            chooser.setCurrentDirectory(filer.getParentFile());
        }
    }

    /**
     * The main method.
     *
     * @param args the command-line arguments
     * @param istest whether this is a test run
     */
    public static void main(String[] args, boolean istest) {
        if (args.length > 0) {
            DEFAULT_INPUT_DIR = args[0];
        }

        if (args.length > 1) {
            DEFAULT_OUTPUT_DIR = args[1];
        } else {
            DEFAULT_OUTPUT_DIR = TopicModelingToolAccessor.getUserHomePath().toString();
        }

        if (args.length > 2) {
            DEFAULT_METADATA_FILE = args[2];
        }

        TopicModelingToolGUI tmt = new TopicModelingToolGUI(istest);
        tmt.go();

        if (istest) {
            tmt.runMallet(tmt.getCheckBoxOptionMap(), tmt.getFieldOptionMap(), 
                tmt.getAdvFieldMap(), tmt.getAdvCheckBoxMap());
        }
    }

    public static void main(String[] args) {
        TopicModelingToolGUI.main(args, false);
    }
}
