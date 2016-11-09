/*
 * BusyDlg.java
 * Created on 14. Mai 2007, 09:33
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.gui;

import jams.gui.tools.GUIHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 *
 * @author S. Kralisch
 */
public class WorkerDlg extends JDialog {

    protected Runnable task;
    protected SwingWorker worker;
    protected Window owner;
    protected JProgressBar progressBar;
    protected JLabel label = null;
    protected JPanel mainPanel = null;

    public WorkerDlg(Window owner, String title) {
        this(owner, title, "");
    }

    public WorkerDlg(Window owner, String title, String message) {
        super(owner);

        this.owner = owner;
        this.setModal(true);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setTitle(title);

        this.setLocationRelativeTo(owner);

        mainPanel = new JPanel(new BorderLayout());
        this.setResizable(false);

        if (!message.equals("")) {
            label = new JLabel(message);
            mainPanel.add(label, BorderLayout.NORTH);
        }

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
        mainPanel.add(progressBar, BorderLayout.CENTER);
        this.add(mainPanel);
        this.pack();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.remove(this.mainPanel);
        this.mainPanel = mainPanel;
        this.add(mainPanel);
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

        if (owner != null) {

            Dimension ownerDim = owner.getSize();
            Dimension thisDim = this.getSize();

            //center dialog window over owner
            int x = (int) owner.getX() + ((ownerDim.width - thisDim.width) / 2);
            int y = (int) owner.getY() + ((ownerDim.height - thisDim.height) / 2);
            setLocation(x, y);
        }

        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("state") && evt.getNewValue() == SwingWorker.StateValue.DONE) {
                    WorkerDlg.this.setVisible(false);
                    WorkerDlg.this.dispose();
                }
            }
        });

        worker.execute();

        this.setVisible(true);
    }

    /**
     * Set a task to be executed This method should only be used when the tasks
     * involves no Swing related methods, since they should only be applied in
     * the event dispatching thread. In this case, use setTask(SwingWorker
     * worker) instead.
     *
     * @param task The task
     */
    public void setTask(Runnable task) {
        this.task = task;

        // put the task into a worker
        worker = new SwingWorker<Object, Void>() {

            public Object doInBackground() {
                WorkerDlg.this.task.run();
                return WorkerDlg.this.task;
            }
        };
    }

    /**
     * Set a task to be executed by creating a SwingWorker. This method should
     * be used if Swing related methods are to be executed. In this case, these
     * should be executed in the done() method of the SwingWorker, i.e. within
     * the event dispatching thread
     *
     * @param worker The SwingWorker
     */
    public synchronized void setTask(SwingWorker worker) {
        this.worker = worker;
    }

    public void setMessage(String text) {
        this.label.setText(text);
        this.invalidate();
        this.pack();
    }
}
