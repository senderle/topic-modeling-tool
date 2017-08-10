package cc.mallet.topics.gui.listeners

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.lang.*;

public class TrainButtonListener implements ActionListener {
    /*
     * Start a new thread that will execute the runMallet method
     */

    Thread t;
    public void actionPerformed(ActionEvent e) {
        // Get current time
        t = new Thread() {
            public void run() {
                if (accessor.getInputDirName().equals("")) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Please select an input file or directory", 
                        "Invalid input", JOptionPane.ERROR_MESSAGE);
                } else {
                    controller.runMallet(fieldOptionMap, checkBoxOptionMap, 
                        advFieldMap, advCheckBoxMap);
                }
            }
        };

        t.start();
    }

}