package cc.mallet.topics.gui.listeners;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

import cc.mallet.topics.gui.TopicModelingToolGUI;

/**
 * The listener interface for receiving advancedButton events.
 * Clicking should bring up the Advanced panel.
 *
 */

public class AdvancedButtonListener implements ActionListener{

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    private TopicModelingToolGUI gui;
    public AdvancedButtonListener(TopicModelingToolGUI gui) {
    	this.gui = gui;
    }
    public void actionPerformed(ActionEvent e) {
    	this.gui.setAdvancedFrame(true);
    	//advancedFrame.setVisible(true);
    }
}