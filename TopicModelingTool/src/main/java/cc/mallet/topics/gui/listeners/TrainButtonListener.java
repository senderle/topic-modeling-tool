package cc.mallet.topics.gui.listeners;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

import cc.mallet.topics.gui.TopicModelingToolController;
import cc.mallet.topics.gui.TopicModelingToolAccessor;
import cc.mallet.topics.gui.TopicModelingToolGUI;

/**
* The listener interface for receiving trainButton events.
*
*/

public class TrainButtonListener implements ActionListener {
    /*
     * Start a new thread that will execute the runMallet method
     */
    public TopicModelingToolController controller;
    public TopicModelingToolAccessor accessor;
    public TopicModelingToolGUI gui;
    public TrainButtonListener(TopicModelingToolController controller,
                               TopicModelingToolAccessor accessor,
                               TopicModelingToolGUI gui) {
        this.controller = controller;
        this.accessor = accessor;
        this.gui = gui;
    }
    Thread t;
    public void actionPerformed(ActionEvent e) {
        // Get current time
        t = new Thread() {
            public void run() {
                if (accessor.getInputDirName().equals("")) {
                    JOptionPane.showMessageDialog(gui.getMainPanel(), 
                        "Please select an input file or directory", 
                        "Invalid input", JOptionPane.ERROR_MESSAGE);
                } else {
                    controller.runMallet(gui.getCheckBoxOptionMap(), gui.getFieldOptionMap(), 
                        gui.getAdvFieldMap(), gui.getAdvCheckBoxMap());
                }
            }
        };

        t.start();
    }

}