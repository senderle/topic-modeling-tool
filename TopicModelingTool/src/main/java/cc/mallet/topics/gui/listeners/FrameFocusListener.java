package cc.mallet.topics.gui.listeners;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

import cc.mallet.topics.gui.TopicModelingToolGUI;

/**
 * The listener interface for receiving frameFocus events.
 * To show a busy hour glass icon when the Basic window is in focus.
 */

public class FrameFocusListener implements FocusListener {

    /* (non-Javadoc)
    * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
    */
    private TopicModelingToolGUI gui;

    public FrameFocusListener(TopicModelingToolGUI gui) {
        this.gui = gui;
    }
    @Override
    public void focusGained(FocusEvent arg0) {
        if (this.gui.getFrameBusy()) {
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            this.gui.setRootFrame(hourglassCursor);
            //rootframe.setCursor(hourglassCursor);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    @Override
    public void focusLost(FocusEvent arg0) {

    }
}