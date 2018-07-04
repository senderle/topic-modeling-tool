package cc.mallet.topics.gui.listeners;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

import cc.mallet.topics.gui.TopicModelingToolGUI;

/**
 * The listener interface for receiving resetButton events.
 */

public class ResetButtonListener implements ActionListener {
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    private TopicModelingToolGUI gui;

    public ResetButtonListener(TopicModelingToolGUI gui) {
    	this.gui = gui;
    }

    public void actionPerformed(ActionEvent e) {
        this.gui.resetAdvControls();
    }
}