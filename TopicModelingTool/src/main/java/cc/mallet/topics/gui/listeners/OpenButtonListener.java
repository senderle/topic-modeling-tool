package cc.mallet.topics.gui.listeners;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import cc.mallet.topics.gui.TopicModelingToolGUI;
import cc.mallet.topics.gui.TopicModelingToolAccessor;

/**
* The listener interface for receiving openButton events. The same interface is used for both 
* the input
* and output directory options
*
*/

public class OpenButtonListener implements ActionListener {
	private JFileChooser filechooser;
	private JTextField filefield;
	private String filedescription;
    private TopicModelingToolGUI gui;
    private TopicModelingToolAccessor accessor;

	public OpenButtonListener(
		JFileChooser filech,
		JTextField filef,
		String filed,
        TopicModelingToolGUI inputGui,
        TopicModelingToolAccessor inputAccessor) {
		this.filechooser = filech;
		this.filefield = filef;
		this.filedescription = filed;
        this.gui = inputGui;
        this.accessor = inputAccessor;
	}

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
    	int returnVal = this.filechooser.showOpenDialog(this.gui.getMainPanel());

    	if (returnVal == JFileChooser.APPROVE_OPTION) {
    		File file = this.filechooser.getSelectedFile();
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

    		this.accessor.appendLog("Chose " + this.filedescription + inputType + inputDir);

    		this.filefield.setText(inputDir);

            this.gui.setChoosers(file);

    		/*for (JFileChooser chooser : allFileChoosers) {
    			chooser.setCurrentDirectory(file.getParentFile());
    		}*/

    	} else {
    		this.accessor.appendLog("Open command cancelled by user.");
    	}
    }
}