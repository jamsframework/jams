/*
 * CancelableWorkerDlg.java
 * Created on 13. Februar 2009, 22:43
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package reg.gui;

import jams.gui.WorkerDlg;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class CancelableWorkerDlg extends WorkerDlg {

    private CancelableSwingWorker task;

    public CancelableWorkerDlg(Frame owner, String title) {
        super(owner, title);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                task.cancel();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelButton);

        this.add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    public synchronized void setTask(CancelableSwingWorker task) {
        this.task = task;
        super.setTask(task);
    }
}
