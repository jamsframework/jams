/*
 * CancelableWorkerDlg.java
 * Created on 13. Februar 2009, 22:43
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.explorer.gui;

import jams.JAMS;
import jams.gui.WorkerDlg;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class CancelableWorkerDlg extends WorkerDlg {
    
    SwingWorker subTask = null;
    
    public CancelableWorkerDlg(Frame owner, String title) {
        super(owner, title);

        JButton cancelButton = new JButton(JAMS.i18n("CANCEL"));
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ((CancelableSwingWorker)task).cancel();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);

        this.add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    public synchronized void setTask(SwingWorker task) {
        subTask = task;
        
        this.task = new CancelableSwingWorker() {            
            @Override
            public int cancel() {
                this.cancel(true);
                return 0;
            }

            @Override
            protected Object doInBackground() throws Exception {
                try{
                    subTask.run();
                }catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        };
        super.setTask(task);
    }
}
