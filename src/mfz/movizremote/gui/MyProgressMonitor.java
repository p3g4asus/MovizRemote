package mfz.movizremote.gui;

import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
public class MyProgressMonitor {
    int total, current = -1;
    boolean indeterminate;
    int milliSecondsToWait = 500; // half second
    String status, name;
    Message moreInfo;

    public MyProgressMonitor(int total, boolean indeterminate,
            int milliSecondsToWait, String name) {
        this.total = total;
        this.indeterminate = indeterminate;
        this.milliSecondsToWait = milliSecondsToWait;
        this.name = name;
    }

    public MyProgressMonitor(int total, boolean indeterminate, String name) {
        this.total = total;
        this.indeterminate = indeterminate;
        this.name = name;
    }

    public int getTotal() {
        return total;
    }

    public void start(String status) {
        if (current != -1)
            throw new IllegalStateException("not started yet");
        this.status = status;
        current = 0;
        fireChangeEvent();
    }

    public int getMilliSecondsToWait() {
        return milliSecondsToWait;
    }

    public int getCurrent() {
        return current;
    }

    public String getStatus() {
        return status;
    }

    public Message getMoreInfo() {
        // TODO Auto-generated method stub
        return moreInfo;
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }

    public void setCurrent(String status, int current, Message moreInfo) {
        if (current == -1)
            throw new IllegalStateException("not started yet");
        this.current = current;
        if (status != null)
            this.status = status;
        if (moreInfo != null)
            this.moreInfo = moreInfo;
        fireChangeEvent();
    }

    /*--------------------------------[ ListenerSupport ]--------------------------------*/

    private Vector<ChangeListener> listeners = new Vector<ChangeListener>();
    private ChangeEvent ce = new ChangeEvent(this);

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireChangeEvent() {
        ChangeListener cl;
        for (int i = listeners.size() - 1; i >= 0; i--) {
            cl = listeners.get(i);
            cl.stateChanged(ce);
        }
        /*
         * Iterator<ChangeListener> iter = listeners.iterator();
         * 
         * while (iter.hasNext()) { iter.next().stateChanged(ce); }
         */
    }

    public String getName() {
        // TODO Auto-generated method stub
        return name;
    }
}
