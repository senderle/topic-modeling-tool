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

    private JTextArea log;

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

    public TopicModelingToolAccessor() {
        this.timestamp = new Date();
        this.useTimeStamp = false;
        this.inputDirTfield = new JTextField();
        this.outputDirTfield = new JTextField();
        this.stopFileField = new JTextField();
        this.metadataFileField = new JTextField();
        this.inputDirAlternate = null;
        this.metadataFileAlternate = null;
        this.failOnExc = isTest;
    }

    public String getInputDirName() {
        if (this.inputDirAlternate == null) {
            return this.inputDirTfield.getText();
        } else {
            return this.inputDirAlternate;
        }
    }

    private void setInputDirAlternate(String in) {
        this.inputDirAlternate = in;
    }

    private void setInputDirAlternate() {
        this.inputDirAlternate = null;
    }

    public String getMetadataFileName() {
        if (this.metadataFileAlternate == null) {
            return this.metadataFileField.getText();
        } else {
            return this.metadataFileAlternate;
        }
    }

    private void setMetadataFileAlternate(String meta) {
        this.metadataFileAlternate = meta;
    }

    private void setMetadataFileAlternate() {
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
    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                this.log.append(text);
                this.log.setCaretPosition(this.log.getDocument().getLength());
            }
        });
    }

    private void appendLog(String... lines) {
        for (String ln : lines) {
            this.log.append(ln);
            this.log.append(NEWLINE);
        }
        this.log.setCaretPosition(this.log.getDocument().getLength());
    }

    private void errorLog(Throwable exc) {
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

    private void setLog(String input) {
        this.log.setText(input);
    }

    private JTextArea getLog() {
        return this.log;
    }

    private void setUpNewLog() {
        this.log = new JTextArea(20, 20);
        this.log.setMargin(new Insets(5, 5, 5, 5));
        this.log.setEditable(false);
    }

    private void setLogCaretPosition() {
        this.log.setCaretPosition(this.log.getDocument().getLength());
    }

    private void enableInputDirTfield(boolean input) {
        this.inputDirTfield.setEnabled(input);
    }

    private void setEditableInputDirTfield(boolean input) {
        this.inputDirTfield.setEditable(input);
    }

    private void setInputDirTfield(String input) {
        this.inputDirTfield.setText(input);
    }

    private JTextField getInputDirTfield() {
        return this.inputDirTfield;
    }

    private void enableOutputDirTfield(boolean input) {
        this.outputDirTfield.setEnabled(input);
    }

    private void setEditableOutputDirTfield(boolean input) {
        this.outputDirTfield.setEditable(input);
    }

    private void setOutputDirTfield(String input) {
        this.outputDirTfield.setText(input);
    }

    private JTextField getOutputDirTfield() {
        return this.outputDirTfield;
    }

    private void enableStopFileField(boolean input) {
        this.stopFileField.setEnabled(input);
    }
    
    private void setEditableStopFileField(boolean input) {
        this.stopFileField.setEditable(input);
    }

    private void setStopFileField(String input) {
        this.stopFileField.setText(input);
    }

    private JTextField getStopFileField() {
        return this.stopFileField;
    }

    private void enableMetadataFileField(boolean input) {
        this.metadataFileField.setEnabled(input);
    }
    
    private void setEditableMetadataFileField(boolean input) {
        this.metadataFileField.setEditable(input);
    }

    private void setMetadataFileField(String input) {
        this.metadataFileField.setText(input);
    }

    private JTextField getMetadataFileField() {
        return this.metadataFileField;
    }

    private void generateDate() {
        this.timestamp = new Date();
    }
}