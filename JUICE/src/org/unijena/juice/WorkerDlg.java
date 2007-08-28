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

package org.unijena.juice;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JFrame;
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
    
    public WorkerDlg(Frame owner, String title) {
        this(owner, title, "");
    }
    
    public WorkerDlg(Frame owner, String title, String message) {
        super(owner);
        //this.setAlwaysOnTop(true);
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
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
        this.add(progressBar, BorderLayout.CENTER);
        
        this.pack();
    }
    
    public void execute() {
        worker.execute();
        setVisible(true);
        try {
            worker.get();
        } catch (InterruptedException ie) {
        } catch (ExecutionException ee) {}
    }
    
    public void setTask(Runnable task) {
        this.task = task;
        worker = new SwingWorker<Object, Void>() {
            public Object doInBackground() {
                WorkerDlg.this.task.run();
                return null;
            }
            public void done() {
                WorkerDlg.this.setVisible(false);
            }
        };
    }
    
    public static void main(String[] args) throws Exception  {
        JFrame frame = new JFrame("TEST");
        frame.setSize(new Dimension(200,200));
        frame.setVisible(true);
        
        Runnable task = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    System.out.println("TEST");
                } catch (Exception e) {}
            }
        };
        
        WorkerDlg dlg = new WorkerDlg(frame, "Bla");
        dlg.setTask(task);
        dlg.execute();
        
    }
    
}
