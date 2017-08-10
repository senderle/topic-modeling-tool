package cc.mallet.topics.gui.listeners

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

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

    		accessor.appendLog("Chose " + filedescription + inputType + inputDir);

    		filefield.setText(inputDir);

    		for (JFileChooser chooser : allFileChoosers) {
    			chooser.setCurrentDirectory(file.getParentFile());
    		}

    	} else {
    		accessor.appendLog("Open command cancelled by user.");
    	}
    }
}