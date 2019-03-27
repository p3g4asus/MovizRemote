package mfz.movizremote.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * MySwing: Advanced Swing Utilites Copyright (C) 2005 Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
public class ProgressUtil {
    static class MonitorListener implements ChangeListener, ActionListener {
        MyProgressMonitor monitor;
        Window owner;
        Timer timer;
        ActionListener cancelListener;
        String name;

        public MonitorListener(Window owner, MyProgressMonitor monitor,
                String name, ActionListener cancList) {
            this.owner = owner;
            this.monitor = monitor;
            this.cancelListener = cancList;
            this.name = name;
        }

        public void stateChanged(ChangeEvent ce) {
            MyProgressMonitor monitor = (MyProgressMonitor) ce.getSource();
            if (monitor.getCurrent() != monitor.getTotal()) {
                if (timer == null) {
                    timer = new Timer(monitor.getMilliSecondsToWait(), this);
                    timer.setRepeats(false);
                    timer.start();
                }
            } else {
                if (timer != null && timer.isRunning())
                    timer.stop();
                monitor.removeChangeListener(this);
            }
        }

        public void actionPerformed(ActionEvent e) {
            monitor.removeChangeListener(this);
            ProgressDialog dlg = owner instanceof Frame ? new ProgressDialog(
                    (Frame) owner, monitor, name, cancelListener)
                    : new ProgressDialog((Dialog) owner, monitor, name,
                            cancelListener);

            dlg.pack();
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
        }
    }

    public static MyProgressMonitor createModalProgressMonitor(Component owner,
            int total, boolean indeterminate, int milliSecondsToWait,
            String name, ActionListener cancelListener) {
        MyProgressMonitor monitor = new MyProgressMonitor(total, indeterminate,
                milliSecondsToWait, name);
        Window window = owner instanceof Window ? (Window) owner
                : SwingUtilities.getWindowAncestor(owner);
        monitor.addChangeListener(new MonitorListener(window, monitor, name,
                cancelListener));
        return monitor;
    }
}
