package cc.mallet.topics.gui;

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
import java.lang.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;

public class TopicModelingToolAccessor {
	/** delimiter constants */
    static final String NEWLINE = "\n";

    private Date timestamp;

    // Currently always false, but the necessary functionality is 
    // fully built-in! 
    private Boolean useTimeStamp;

    public JTextArea log;

    private JTextField inputDirTfield;
    private JTextField outputDirTfield;
    private JTextField stopFileField;
    private JTextField metadataFileField;

    private String inputDirAlternate;
    private String metadataFileAlternate;

    private Boolean failOnExc;

    // ////////////////////////////////////////////////// //
    // SECTION ONE: Small Utility Functions and Accessors //
    // ////////////////////////////////////////////////// //

    public TopicModelingToolAccessor(boolean isTest) {
        this.timestamp = new Date();
        this.useTimeStamp = false;
        this.inputDirTfield = new JTextField();
        this.outputDirTfield = new JTextField();
        this.stopFileField = new JTextField();
        this.metadataFileField = new JTextField();
        this.inputDirAlternate = null;
        this.metadataFileAlternate = null;
        this.failOnExc = isTest;
        log = null;
    }

    public TopicModelingToolAccessor() {
        this(false);
    }

    public String getInputDirName() {
        if (this.inputDirAlternate == null) {
            return this.inputDirTfield.getText();
        } else {
            return this.inputDirAlternate;
        }
    }

    public void setInputDirAlternate(String in) {
        this.inputDirAlternate = in;
    }

    public void setInputDirAlternate() {
        this.inputDirAlternate = null;
    }

    public String getMetadataFileName() {
        if (this.metadataFileAlternate == null) {
            return this.metadataFileField.getText();
        } else {
            return this.metadataFileAlternate;
        }
    }

    public void setMetadataFileAlternate(String meta) {
        this.metadataFileAlternate = meta;
    }

    public void setMetadataFileAlternate() {
        this.metadataFileAlternate = null;
    }

    public String getOutputDir() {
        return this.outputDirTfield.getText();
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
        if (this.useTimeStamp) {
    	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH.mm") ;
    	    String name = dateFormat.format(this.timestamp);
            dir = Paths.get(getOutputDir(), "output-" + name);
        } else {
            dir = Paths.get(getOutputDir());
        }
        dir.toFile().mkdirs();
    	return dir.toString();
    }
    
    public String getStopFileName() {
        return this.stopFileField.getText();
    }

    /**
     * Update text area.
     *
     * @param text the text
     */
    public void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                log.append(text);
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    public void appendLog(String... lines) {
        for (String ln : lines) {
            log.append(ln);
            log.append(NEWLINE);
        }
        log.setCaretPosition(log.getDocument().getLength());
    }

    public void errorLog(Throwable exc) {
        if (this.failOnExc) {
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

    public static String escapeTab(String in) {
        return in.replace("\\t", "\t");
    }

    public String formatMalletCommand(String cmd, String[] args) {
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
    public void redirectSystemStreams() {
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

    public void setLog(String input) {
        log.setText(input);
    }

    public JTextArea getLog() {
        return log;
    }

    public void setUpNewLog() {
        log = new JTextArea(20, 20);
        log.setMargin(new Insets(5, 5, 5, 5));
        log.setEditable(false);
    }

    public void setLogCaretPosition() {
        log.setCaretPosition(log.getDocument().getLength());
    }

    public void enableInputDirTfield(boolean input) {
        this.inputDirTfield.setEnabled(input);
    }

    public void setEditableInputDirTfield(boolean input) {
        this.inputDirTfield.setEditable(input);
    }

    public void setInputDirTfield(String input) {
        this.inputDirTfield.setText(input);
    }

    public JTextField getInputDirTfield() {
        return this.inputDirTfield;
    }

    public void enableOutputDirTfield(boolean input) {
        this.outputDirTfield.setEnabled(input);
    }

    public void setEditableOutputDirTfield(boolean input) {
        this.outputDirTfield.setEditable(input);
    }

    public void setOutputDirTfield(String input) {
        this.outputDirTfield.setText(input);
    }

    public JTextField getOutputDirTfield() {
        return this.outputDirTfield;
    }

    public void enableStopFileField(boolean input) {
        this.stopFileField.setEnabled(input);
    }
    
    public void setEditableStopFileField(boolean input) {
        this.stopFileField.setEditable(input);
    }

    public void setStopFileField(String input) {
        this.stopFileField.setText(input);
    }

    public JTextField getStopFileField() {
        return this.stopFileField;
    }

    public void enableMetadataFileField(boolean input) {
        this.metadataFileField.setEnabled(input);
    }
    
    public void setEditableMetadataFileField(boolean input) {
        this.metadataFileField.setEditable(input);
    }

    public void setMetadataFileField(String input) {
        this.metadataFileField.setText(input);
    }

    public JTextField getMetadataFileField() {
        return this.metadataFileField;
    }

    public void generateDate() {
        this.timestamp = new Date();
    }
}