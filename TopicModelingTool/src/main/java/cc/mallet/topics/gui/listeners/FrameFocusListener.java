package cc.mallet.topics.gui.listeners

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

public class FrameFocusListener implements FocusListener {

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