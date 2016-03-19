package cc.mallet.topics.gui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;

/**
 * The Class TopicModelingGUI.
 */
public class TopicModelingTool {
    /** the delimiter for csv files */
    public static final String CSV_DEL = ",";

    /** filename constants */
    public static final String TOPIC_WORDS = "TopicWords.csv";
    public static final String DOCS_IN_TOPICS = "DocsInTopics.csv";
    public static final String TOPICS_IN_DOCS_VECTORS = "TopicsInDocsVectors.csv";
    public static final String TOPICS_IN_DOCS = "TopicsInDocs.csv";

    /** used for testing to set a input dir on startup */
    public static String DEFAULT_INPUT_DIR = null;
    private static final long serialVersionUID = 1L;

    static private final String newline = "\n";

    JFrame rootframe, advancedFrame;
    JButton inputDataButton, trainButton, advancedButton,
            stopChooseButton, outputDirButton, clearButton;
    JTextArea log;
    JPanel mainPanel, advPanel;
    JTextField inputDirTfield = new JTextField();
    JTextField outputDirTfield = new JTextField();
    JTextField numTopics = new JTextField(2);

    JTextField stopFileField = new JTextField("Mallet Default");
    LinkedHashMap<String, String[]> checkBoxOptionMap = new LinkedHashMap<String, String[]>();
    LinkedHashMap<String, String[]> fieldOptionMap = new LinkedHashMap<String, String[]>();
    HashMap<String, String> otherOptionsMap = new HashMap<String, String>();
    JCheckBox stopBox;
    ArrayList<JCheckBox> advCheckBoxList = new  ArrayList<JCheckBox>();
    ArrayList<JTextField> advFieldList = new ArrayList<JTextField>();
    JDialog helpPane1, helpPane2;
    Boolean frameBusy = false;


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
    public void showHelp1(){
        helpPane1.setVisible(true);
    }

    /**
     * Show help in Advanced panel.
     */
    public void showHelp2(){
        helpPane2.setVisible(true);
    }

    /**
     * Creates the help panel in the Advanced window.
     */
    public void createHelp2()
    {
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
    * Listener interface for help request in the Basic window.
    */

    public class BasicHelpListener implements MouseListener{

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            showHelp1();

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseExited(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            // TODO Auto-generated method stub
        }
    }

    /**
     *Listener interface for help requests in the Advanced window.
     */

    public class AdvHelpListener implements MouseListener {

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            showHelp2();

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseExited(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            // TODO Auto-generated method stub

        }
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
                String inputDir = file.getPath();
                String inputType = "";
                
                if (file.isDirectory()) {
                    inputType = " Directory: ";
                } else {
                    inputType = " File: ";
                }

                log.append("Chose " + filedescription + inputType + inputDir + newline);
               
                filefield.setText(inputDir);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
  
    /**
     * The listener interface for receiving stopBox events.
     *
     */
    public class StopBoxListener implements ActionListener{
   
       /**
        * When the stopwords checkbox is checked, enable the stopword file button.
        */
       public void actionPerformed(ActionEvent e) {
            if(stopBox.isSelected()){
                 stopChooseButton.setEnabled(true);
             }
   
            else{
                stopChooseButton.setEnabled(false);
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
                    runMallet();
                }
            };
            t.start();
        }

        /**
         * Method that assembles all the options given by the user through the GUI
         * and runs Mallet's importing and topic modeling methods.
         */
        public void runMallet() {
            long start = System.currentTimeMillis();
            if (inputDirTfield.getText().equals("")) {
                JOptionPane.showMessageDialog(mainPanel, "Please select an input file or directory", "Invalid input", JOptionPane.ERROR_MESSAGE);
            } else {
                clearButton.setEnabled(false);
                trainButton.setEnabled(false);
        
                String outputDir = outputDirTfield.getText();
                String collectionPath = new File(outputDir, "topic-input.mallet").getPath();  //FIXME
                String inputDir = inputDirTfield.getText();
                File file = new File(inputDir);
                log.append("Importing and Training...this may take a few minutes depending on collection size.");
        
                log.append("Importing from: " + inputDir + "." + newline);
                log.setCaretPosition(log.getDocument().getLength());
        
                ////////////////
                try {
        
                    Class<?> malletImportClass, malletTrainingClass;
        
                    if(file.isDirectory())
                        malletImportClass = Class.forName("cc.mallet.classify.tui.Text2Vectors");
                    else
                        malletImportClass = Class.forName("cc.mallet.classify.tui.Csv2Vectors");
        
                    String[] advArgs = getAdvArgs();
                    ArrayList<String> imp = new ArrayList<String>();
                    ArrayList<String> trn = new ArrayList<String>();
                    for(int i = 0; i < advArgs.length; i = i + 2) {
                        if(fieldOptionMap.containsKey(advArgs[i])) {
                            if(fieldOptionMap.get(advArgs[i])[2].equals("import")) {
                                imp.add(advArgs[i]);
                                imp.add(advArgs[i + 1]);
                            } else if(fieldOptionMap.get(advArgs[i])[2].equals("train")) {
                                trn.add(advArgs[i]);
                                trn.add(advArgs[i + 1]);
                            }
                        } else if(checkBoxOptionMap.containsKey(advArgs[i])) {
                            if(checkBoxOptionMap.get(advArgs[i])[2].equals("import")) {
                                imp.add(advArgs[i]);
                                imp.add(advArgs[i + 1]);
                            } else if(checkBoxOptionMap.get(advArgs[i])[2].equals("train")) {
                                trn.add(advArgs[i]);
                                trn.add(advArgs[i + 1]);
                            }
                        } else if(otherOptionsMap.containsKey(advArgs[i])) {
                            if(otherOptionsMap.get(advArgs[i]).equals("import")) {
                                imp.add(advArgs[i]);
                                imp.add(advArgs[i + 1]);
                            } else if(otherOptionsMap.get(advArgs[i]).equals("train")) {
                                trn.add(advArgs[i]);
                                trn.add(advArgs[i + 1]);
                            }
                        }
                    }
        
                    // TODO: Replace dumb comment-headed blocks with actual functions.
        
                    //////////////////////
                    // IMPORTING FILES: //
                    //////////////////////
        
                    String[] temp1 = {"--input", inputDir, "--output", collectionPath, "--keep-sequence"};
                    Collections.addAll(imp, temp1);
                    String[] fullImportArgs = imp.toArray(new String[imp.size()]);
        
                    // Now invoke the method.
                    Class<?>[] argTypes =  {fullImportArgs.getClass(), }; // array is Object!
                    Method malletImportMain = malletImportClass.getMethod("main", argTypes);
                    Object[] passedArgs =  {fullImportArgs};
                    System.out.println(Arrays.toString(fullImportArgs));
        
                    Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
                    rootframe.setCursor(hourglassCursor);
                    frameBusy = true;
        
                    trainButton.setText("Importing...");
                    malletImportMain.invoke(null, passedArgs);
        
                    /////////////////////
                    // TRAINING MODEL: //
                    /////////////////////
        
                    String stateFile = outputDir + File.separator + "output_state.gz";
                    String outputDocTopicsFile = outputDir + File.separator + "output_doc_topics.txt";
                    String topicKeysFile = outputDir + File.separator + "output_topic_keys";
                    malletTrainingClass = Class.forName("cc.mallet.topics.tui.Vectors2Topics");
        
                    String[] temp2 = {"--input", collectionPath, "--num-topics", numTopics.getText(),
                            "--output-state", stateFile, "--output-topic-keys", topicKeysFile, "--output-doc-topics", outputDocTopicsFile};
                    Collections.addAll(trn, temp2);
                    String[] fullTrainArgs = trn.toArray(new String[trn.size()]);
                    // Now invoke the method.
                    argTypes = new Class<?>[] {fullTrainArgs.getClass(), }; // array is Object!
        
                    Method malletTrainingMain = malletTrainingClass.getMethod("main", argTypes);
                    passedArgs =  new  Object[]{fullTrainArgs};
                    System.out.println(Arrays.toString(fullTrainArgs));
                    System.out.println("STOP!");
        
                    trainButton.setText("Training...");
        
                    malletTrainingMain.invoke(null, passedArgs);
        
                    ////////////////////////
                    // GENERATING OUTPUT: //
                    ////////////////////////
        
                    GunZipper g = new GunZipper(new File(stateFile));
                    g.unzip(new File(outputDir + File.separator + "output_state"));
        
                    outputCsvFiles(outputDir, true);
        
                    System.out.println("Mallet Output files written in " + outputDir + " ---> " + stateFile + " , " +
                            topicKeysFile + newline);
                    System.out.println("Csv Output files written in " + outputDir + File.separator+ "output_csv");
                    System.out.println("Html Output files written in " + outputDir + File.separator+ "output_html");

                } catch (Throwable e1) {
                    e1.printStackTrace();
                }

                log.setCaretPosition(log.getDocument().getLength());
                clearButton.setEnabled(true);
        
                long elapsedTimeMillis = System.currentTimeMillis()-start;
        
                // Get elapsed time in seconds
                float elapsedTimeSec = elapsedTimeMillis/1000F;
                System.out.println("Time :" + elapsedTimeSec);
        
                Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        
                trainButton.setText("Learn Topics");
                trainButton.setEnabled(true);
        
                rootframe.setCursor(normalCursor);
                frameBusy = false;

            }
        }

        /**
        * Output csv files.
        *
        * @param outputDir the output directory
        * @param htmlOutputFlag print html output or not
        */
        private void outputCsvFiles(String outputDir, Boolean htmlOutputFlag)
        {

            CsvBuilder cb = new CsvBuilder();
            cb.createCsvFiles(Integer.parseInt(numTopics.getText()), outputDir);
            if (htmlOutputFlag) {
                HtmlBuilder hb = new HtmlBuilder(cb.getNtd(), new File(inputDirTfield.getText()));
                hb.createHtmlFiles(new File(outputDir));
            }
            clearExtrafiles(outputDir);
        }

        private void clearExtrafiles(String outputDir)
        {
            String[] fileNames = {"topic-input.mallet", "output_topic_keys", "output_state.gz",
                                    "output_doc_topics.txt", "output_state"};
            for (String f:fileNames) {
                if (!(new File(outputDir, f).canWrite())) {
                    System.out.println(f);
                }
                Boolean b = new File(outputDir, f).delete();
            }
        }
    }

    /**
     * Clear console area
     */
    public class ClearButtonListener implements ActionListener{

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
    public class ResetButtonListener implements ActionListener{
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
    public void setDefaultOptions() 
    {
        checkBoxOptionMap.put("--remove-stopwords", new String[]{"Remove stopwords ", "TRUE", "import"});
        checkBoxOptionMap.put("--preserve-case", new String[]{"Case sensitive ", "FALSE", "import"});
        fieldOptionMap.put("--num-iterations", new String[]{"No. of iterations ", "400", "train"});
        fieldOptionMap.put("--num-top-words", new String[]{"No. of topic words printed ", "10", "train"});
        fieldOptionMap.put("--doc-topics-threshold", new String[]{"Topic proportion threshold ", "0.05", "train"});
        fieldOptionMap.put("--optimize-interval", new String[]{"Prior optimization interval ", "0", "train"});
        otherOptionsMap.put("--stoplist-file", new String("import"));
    }
  
    /**
     * Initializes the advanced controls.
     */
    public void initAdvControls()
    {
        for(String k:fieldOptionMap.keySet()) {
            JTextField tempField = new JTextField(fieldOptionMap.get(k)[1]);
            advFieldList.add(tempField);
        }
  
        for(String k:checkBoxOptionMap.keySet()) {
            JCheckBox tempCheckBox = new JCheckBox(checkBoxOptionMap.get(k)[0]);
            if(checkBoxOptionMap.get(k)[1].equals("TRUE")) {
                tempCheckBox.setSelected(true);
            }

            advCheckBoxList.add(tempCheckBox);

            if(k.equals("--remove-stopwords")) {
                tempCheckBox.addActionListener(new StopBoxListener());
                stopBox = tempCheckBox;
            }
        }
    }
  
    /**
     * Reset advanced controls to default values.
     */
    public void resetAdvControls() 
    {
        Iterator<JTextField> itr1 = advFieldList.iterator();
        for(String[] k:fieldOptionMap.values()) {
            itr1.next().setText(k[1]);
        }
  
        Iterator<JCheckBox> itr2 = advCheckBoxList.iterator();
        for (String[] k:checkBoxOptionMap.values()) {
            JCheckBox jc = itr2.next();
            if (k[1].equals("TRUE")) {
                jc.setSelected(true);
            } else {
                jc.setSelected(false);
            }
        }
  
        stopFileField.setText("Mallet Default");
        stopChooseButton.setEnabled(stopBox.isSelected());        // Not sure why this doesn't happen automatically
    }
  
    /**
     * Gets the adv args.
     *
     * @return the adv args
     */
    public String[] getAdvArgs(){
  
        String[] advArgs = new String[(checkBoxOptionMap.size() + fieldOptionMap.size() + 1) * 2];  // + 1 for stopword list
        int index = 0;
  
        Iterator<JCheckBox> itr1 = advCheckBoxList.iterator();
        for(String k:checkBoxOptionMap.keySet()) {
            advArgs[index] = k;
            boolean b =  itr1.next().isSelected();
            advArgs[index + 1] = new Boolean(b).toString();
            index = index + 2;
        }
  
        Iterator<JTextField> itr2 = advFieldList.iterator();
        for(String k:fieldOptionMap.keySet()) {
            advArgs[index] = k;
            if (k.equals("--num-top-words")) {
                int topwords =  Integer.parseInt(itr2.next().getText()) + 1;     //spl case - potential mallet bug
                advArgs[index + 1] = new Integer(topwords).toString();
            }
            else {
                advArgs[index + 1] =  itr2.next().getText();
            }
            index = index + 2;
        }
  
        //
        if (stopFileField.getText().equals("Mallet Default")) {
            advArgs[index] = "";
            advArgs[index + 1] = "";
        } else {
            advArgs[index] = "--stoplist-file";
            advArgs[index + 1] = stopFileField.getText();
        }
        return advArgs;
    }
  
    /**
     * Builds the advanced panel.
     */
    public void buildAdvPanel()
    {
        //create new advanced options window
        advancedFrame = new JFrame("TopicModelingTool");
        //advancedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        advPanel = new JPanel(new BorderLayout());
  
        Box advCheckBoxPanel = new Box(BoxLayout.Y_AXIS);
  
        advCheckBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        advCheckBoxPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 5));

        for (JCheckBox tempCheckBox:advCheckBoxList) {
            advCheckBoxPanel.add(tempCheckBox);
            tempCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
  
        JPanel advFieldPanel = new JPanel(new GridLayout(fieldOptionMap.size(), 2));
        advFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Collection<String[]> c = fieldOptionMap.values();
        Iterator<String[]> itr = c.iterator();
        advFieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        for (JTextField tempField:advFieldList) {
            advFieldPanel.add(new Label(itr.next()[0]));
            advFieldPanel.add(tempField);
        }
  
        Box advBox = new Box(BoxLayout.Y_AXIS);
        advBox.add(advCheckBoxPanel);
        advBox.add(advFieldPanel);
  
        advPanel.add(advBox, BorderLayout.CENTER);
  
        
        stopFileField.setEnabled(false);

        JFileChooser stopChooser = new JFileChooser();
        stopChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        stopChooser.setCurrentDirectory(new File("."));
        FileFilter filter = null;
        stopChooser.setFileFilter(filter);
        stopChooseButton = new JButton("Stopword File...",
                createImageIcon("/images/Open16.gif"));
        stopChooseButton.addActionListener(
                new OpenButtonListener(stopChooser, stopFileField, "Stopword"));

        JPanel fcPanel = new JPanel(new GridLayout(1, 3));
        fcPanel.add(stopFileField);
        fcPanel.add(stopChooseButton);
        fcPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  
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

        JFileChooser inputfc = new JFileChooser();
        inputfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        inputfc.setCurrentDirectory(new File("."));

        inputDirTfield.setColumns(20);
        inputDirTfield.setEnabled(false);
        if (DEFAULT_INPUT_DIR != null) {
            inputDirTfield.setText(DEFAULT_INPUT_DIR);
        }

        inputDataButton = new JButton("Select Input File or Dir",
                                      createImageIcon("/images/Open16.gif"));
        inputDataButton.addActionListener(
                new OpenButtonListener(inputfc, inputDirTfield, "Input"));

        JPanel inputPanel = new JPanel();
        inputPanel.add(inputDirTfield);
        inputPanel.add(inputDataButton);

        //// Output File Chooser ////

        JFileChooser outputfc = new JFileChooser();
        outputfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputfc.setCurrentDirectory(new File("."));

        outputDirTfield.setText(outputfc.getCurrentDirectory().getPath());
        outputDirTfield.setEnabled(false);

        outputDirButton = new JButton("Select Output Dir",
                createImageIcon("/images/Open16.gif"));
        outputDirButton.addActionListener(
                new OpenButtonListener(outputfc, outputDirTfield, "Output"));

        JPanel outputPanel = new JPanel();
        outputPanel.add(outputDirTfield);
        outputPanel.add(outputDirButton);
  
        //// Advanced Button and Number of Topics ////

        advancedButton = new JButton("Advanced...");
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
        buttonBox.add(inputPanel);
        buttonBox.add(outputPanel);
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
        rootframe.setVisible(true);
        createHelp1();
        createHelp2();
    }
  
    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        if (args.length>0) {
            DEFAULT_INPUT_DIR = args[0];
        }
        new TopicModelingTool().go();
    }
}
