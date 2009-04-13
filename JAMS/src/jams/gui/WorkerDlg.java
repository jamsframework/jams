/*
 * BusyDlg.java
 * Created on 14. Mai 2007, 09:33
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.gui;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 *
 * @author S. Kralisch
 */
public class WorkerDlg extends JDialog {

    private Runnable task;

    private SwingWorker worker;

    private Frame owner;

    private JProgressBar progressBar;

    public WorkerDlg(Frame owner, String title) {
        this(owner, title, "");
    }

    public WorkerDlg(Frame owner, String title, String message) {
        super(owner);

        this.owner = owner;
        this.setModal(true);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setTitle(title);

        this.setLocationRelativeTo(owner);

        this.setLayout(new BorderLayout());
        this.setResizable(false);

        if (!message.equals("")) {
            JLabel label = new JLabel(message);
            this.add(label, BorderLayout.NORTH);
        }

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
        this.add(progressBar, BorderLayout.CENTER);

        this.pack();
    }

    public void setInderminate(boolean value) {
        if (value) {
            progressBar.setStringPainted(false);
        } else {
            progressBar.setStringPainted(true);
        }
        progressBar.setIndeterminate(value);
    }

    public void setProgress(int value) {
        progressBar.setValue(value);
    }

    public void setProgressMax(int value) {
        progressBar.setMaximum(value);
    }

    public void execute() {

        Dimension ownerDim = owner.getSize();
        Dimension thisDim = this.getSize();

        //center dialog window over owner
        int x = (int) owner.getX() + ((ownerDim.width - thisDim.width) / 2);
        int y = (int) owner.getY() + ((ownerDim.height - thisDim.height) / 2);
        setLocation(x, y);

        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("state") && evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    WorkerDlg.this.setVisible(false);
                    WorkerDlg.this.dispose();
                }
            }
        });

        // @TODO: fix occasional problems when task contains swing operations
        // (e.g. repaints)
        worker.execute();
        this.setVisible(true);
        
    }

    public void setTask(Runnable task) {
        this.task = task;
        worker = new SwingWorker<Object, Void>() {

            public Object doInBackground() {
                //WorkerDlg.this.setVisible(true);

                try {
                    WorkerDlg.this.task.run();
                } catch (Throwable t) {
                    JAMS.handle(t);
                }
                return null;
            }

//            public void done() {
//                WorkerDlg.this.setVisible(false);
//            }
        };
    }
}
