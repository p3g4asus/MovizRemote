package mfz.movizremote.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import mfz.movizremote.utils.Message;

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
public class ProgressDialog extends JDialog implements ChangeListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 5130556785060551462L;
    JLabel statusLabel = new JLabel();
    MessagePane messagePN = new MessagePane();
    JProgressBar progressBar = new JProgressBar();
    MyProgressMonitor monitor;
    ActionListener cancelListener;

    public ProgressDialog(Dialog owner, MyProgressMonitor monitor, String name,
            ActionListener cancList) throws HeadlessException {
        super(owner, "Progress", true);
        setName(name);
        init(monitor, cancList);
    }

    public ProgressDialog(Frame owner, MyProgressMonitor monitor, String name,
            ActionListener cancList) throws HeadlessException {
        super(owner);
        setName(name);
        init(monitor, cancList);
    }

    private void init(MyProgressMonitor monitor, ActionListener cancList) {
        this.monitor = monitor;
        this.cancelListener = cancList;

        progressBar = new JProgressBar(0, monitor.getTotal());
        if (monitor.isIndeterminate())
            progressBar.setIndeterminate(true);
        else
            progressBar.setValue(monitor.getCurrent());
        statusLabel.setText(monitor.getStatus());

        JPanel wcontents = (JPanel) getContentPane();
        JPanel contents = new JPanel();
        contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contents.setLayout(new BorderLayout());
        contents.add(statusLabel, BorderLayout.NORTH);
        contents.add(progressBar);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        if (cancList != null) {
            final JButton cancelButton = new JButton();
            cancelButton.setName(getName());
            cancelButton.setText(PRMessages.getString(getName()
                    + ".progress.abortBTN"));
            cancelButton.addActionListener(cancelListener);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    cancelListener.actionPerformed(new ActionEvent(
                            cancelButton, ActionEvent.ACTION_FIRST, "press"));
                }
            });
            JPanel buttPanel = new JPanel();
            buttPanel.setLayout(new FlowLayout());
            buttPanel.add(cancelButton);
            contents.add(buttPanel, BorderLayout.SOUTH);
        }
        wcontents.add(contents, BorderLayout.NORTH);
        messagePN.setEditable(false); // set textArea non-editable
        messagePN.setPreferredSize(new Dimension(500, 200));
        DefaultCaret caret = (DefaultCaret) messagePN.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        JScrollPane scroll = new JScrollPane(messagePN);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        ;
        wcontents.add(scroll, BorderLayout.CENTER);
        // setUndecorated(true);
        setTitle(PRMessages.getString(getName() + ".progress.title"));
        monitor.addChangeListener(this);
    }

    public void stateChanged(final ChangeEvent ce) {
        // to ensure EDT thread
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    stateChanged(ce);
                }
            });
            return;
        }

        if (monitor.getCurrent() != monitor.getTotal()) {
            statusLabel.setText(monitor.getStatus());
            Message m = monitor.getMoreInfo();
            if (m != null)
                messagePN.updateValues(m);
            if (!monitor.isIndeterminate())
                progressBar.setValue(monitor.getCurrent());
        } else
            dispose();
    }
}
