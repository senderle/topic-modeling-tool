package cc.mallet.topics.gui.listeners;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

import cc.mallet.topics.gui.TopicModelingToolAccessor;

/**
 * Clear console area
 */

public class ClearButtonListener implements ActionListener {

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    private TopicModelingToolAccessor accessor;

    public ClearButtonListener(TopicModelingToolAccessor accessor) {
    	this.accessor = accessor;
    }

    public void actionPerformed(ActionEvent e) {
        this.accessor.setLog("");
    }
}